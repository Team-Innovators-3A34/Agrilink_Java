package org.example.demo.services.ressource;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

public class SmsService {
    public static final String ACCOUNT_SID = "AC840ab9c31d19f27af8df3c068744c874";
    public static final String AUTH_TOKEN = "7f70ebb539cd597e1c87d0235b0ec1b9" ;

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public static void envoyerSms(String numeroDestinataire, String contenu) {
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber(numeroDestinataire),
                new com.twilio.type.PhoneNumber("+19148751979"),
                contenu
        ).create();

        System.out.println("✅ SMS envoyé ! SID: " + message.getSid());
    }
}

