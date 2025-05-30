package com.joelkanyi.mealtime.api.mealtimeapi.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val userDetailsService: UserDetailsService,
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    @Throws(IllegalArgumentException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        // Check if header is null, doesn't start with Bearer, or is "Bearer null"
        if (authHeader == null || !authHeader.startsWith("Bearer ") || authHeader == "Bearer null") {
            filterChain.doFilter(request, response)
            return
        }

        val jwtToken: String = authHeader.substring(7)
        
        // Additional check to ensure token is not empty or "null" string
        if (jwtToken.isBlank() || jwtToken == "null") {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val userEmail: String = jwtService.extractUserName(jwtToken)

            if (userEmail.isNotEmpty() && SecurityContextHolder.getContext().authentication == null) {
                val userDetails = this.userDetailsService.loadUserByUsername(userEmail)
                if (jwtService.isTokenValid(jwtToken, userDetails)) {
                    val authToken = UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.authorities
                    )
                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
        } catch (e: Exception) {
            // Log the exception if needed, but don't break the filter chain
            // This allows the request to continue to the controller where it will be handled appropriately
            logger.debug("JWT token validation failed: ${e.message}")
        }
        
        filterChain.doFilter(request, response)
    }
}