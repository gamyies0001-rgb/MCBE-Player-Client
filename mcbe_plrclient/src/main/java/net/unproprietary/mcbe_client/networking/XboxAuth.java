package net.unproprietary.mcbe_client.networking;

import com.microsoft.aad.msal4j.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class XboxAuth {
	public static String authenticate() {
		String clientId = "000000004c12ae29"; // standard CID
		String authority = "https://login.microsoftonline.com/consumers/";
		Set<String> scopes = Collections.singleton("XboxLIve.signin offline_access");
		
		try {
			PublicClientApplication app = PublicClientApplication.builder(clientId)
					.authority(authority)
					.build();
			
			System.out.println("Initialising Xbox Live Login Flow...");
			
			DeviceCodeFlowParameters params = DeviceCodeFlowParameters.builder(scopes, deviceCode -> {
				// This prints the URL (microsoft.com/link) and the unique 8-digit code to the terminal
                System.out.println("\n=================================================");
                System.out.println(deviceCode.message());
                System.out.println("^ Click this link to sign in.");
                System.out.println("=================================================\n");
			}).build();
			
			IAuthenticationResult r = app.acquireToken(params).join();
			
			System.out.println("Success! Microsoft Access Token obtained.");
            System.out.println("Token: " + r.accessToken().substring(0, 15) + "...");
            return r.accessToken();
            
		} catch (Exception e) {
			System.err.println("Authentication failed: " + e.getMessage());
		}
		
		return "Fail";
	}
}
