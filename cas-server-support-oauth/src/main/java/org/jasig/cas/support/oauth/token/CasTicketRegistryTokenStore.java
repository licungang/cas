package org.jasig.cas.support.oauth.token;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;

public class CasTicketRegistryTokenStore implements TokenStore {
    
    @NotNull
    private TicketRegistry ticketRegistry;
    
    @NotNull
    private TokenExpirationConfig tokenExpirationConfig;

    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        return readAuthentication(token.getValue());
    }

    @Override
    public OAuth2Authentication readAuthentication(String token) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        // TODO Auto-generated method stub

    }
    
    @Override
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        
        try {
            TicketGrantingTicket ticket = ticketRegistry.getTicket(tokenValue, TicketGrantingTicket.class);
            
            if (ticket == null) {
                throw new InvalidTicketException("Ticket not found in ticket registry");
            }
            
            long remainingValidSeconds = 
                    System.currentTimeMillis() - 
                    ticket.getCreationTime() - 
                    TimeUnit.SECONDS.toMillis(tokenExpirationConfig.getAccessTokenValiditySeconds());
            
            return new CasTGTOAuth2AccessToken(ticket, remainingValidSeconds);
        } catch (InvalidTicketException e) {
            return null;
        }
    }

    /**
     * Use this method to remove the TGT found in the access token from the 
     * ticket registry
     */
    @Override
    public void removeAccessToken(OAuth2AccessToken token) {
        String casTGTValue = token.getValue();
        ticketRegistry.deleteTicket(casTGTValue);
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
        // TODO Auto-generated method stub

    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String tokenValue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken token) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        // TODO Auto-generated method stub

    }

    /**
     * The access token itself isn't really stored, but it's converted from a 
     * CAS TGT retrieved from the ticket registry
     */
    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof SimplePrincipal)) {
            return null;
        }
        String casUsername = ((SimplePrincipal) authentication.getPrincipal()).getId();
        
        OAuth2AccessToken accessToken = null; 
        
        for (Ticket casTicket: ticketRegistry.getTickets()) {
            if (casTicket instanceof TicketGrantingTicket) {
                TicketGrantingTicket casTicketGrantingTicket = (TicketGrantingTicket) casTicket;
                if (casTicketGrantingTicket.getAuthentication().getPrincipal().getId().equals(casUsername)) {
                    accessToken = createAccessTokenFromTGT(casTicketGrantingTicket);
                    break;
                }
            }
        }
        
        return accessToken;
    }

    /**
     * Returns all TGT OAuth tokens whose username matches the one assigned for that TGT
     */
    @Override
    public Collection<OAuth2AccessToken> findTokensByUserName(String userName) {
        Collection<OAuth2AccessToken> accessTokens = new ArrayList<OAuth2AccessToken>();
        for (Ticket casTicket: ticketRegistry.getTickets()) {
            if (casTicket instanceof TicketGrantingTicket) {
                TicketGrantingTicket casTicketGrantingTicket = (TicketGrantingTicket) casTicket;
                if (casTicketGrantingTicket.getAuthentication().getPrincipal().getId().equals(userName)) {
                    accessTokens.add(createAccessTokenFromTGT((TicketGrantingTicket) casTicket));
                }
            }
        }
        return accessTokens;
    }

    /**
     * This function assumes it's used to find all tokens that are valid for 
     * the specified client. Since all clients are valid for all tokens, 
     * return the list of all access tokens.
     */
    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        Collection<OAuth2AccessToken> accessTokens = new ArrayList<OAuth2AccessToken>();
        for (Ticket casTicket: ticketRegistry.getTickets()) {
            if (casTicket instanceof TicketGrantingTicket) {
                accessTokens.add(createAccessTokenFromTGT((TicketGrantingTicket) casTicket));
            }
        }
        return accessTokens;
    }
    
    private OAuth2AccessToken createAccessTokenFromTGT(TicketGrantingTicket ticket) {
        return new CasTGTOAuth2AccessToken(ticket, 
                tokenExpirationConfig.getAccessTokenValiditySeconds());
    }

    public void setTicketRegistry(TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    /**
     * @param tokenExpirationConfig the tokenExpirationConfig to set
     */
    public void setTokenExpirationConfig(TokenExpirationConfig tokenExpirationConfig) {
        this.tokenExpirationConfig = tokenExpirationConfig;
    }

}
