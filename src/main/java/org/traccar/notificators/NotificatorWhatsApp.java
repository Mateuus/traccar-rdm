/*
 * Copyright 2019 - 2020 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.notificators;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.config.Keys;
import org.traccar.model.Event;
import org.traccar.model.User;
import org.traccar.model.Position;
import org.traccar.notification.NotificationFormatter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.client.InvocationCallback;
import java.nio.charset.StandardCharsets;

public class NotificatorWhatsApp extends Notificator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificatorWhatsApp.class);

    private final String url;

    public static class Message {
        @JsonProperty("chatId")
        private String chatId;
        @JsonProperty("text")
        private String text;
    }

    public NotificatorWhatsApp() {
        url = Context.getConfig().getString(Keys.NOTIFICATOR_WHATSPP_URL);
    }

    @Override
    public void sendSync(long userId, Event event, Position position) {
        final User user = Context.getPermissionsManager().getUser(userId);
        Message message = new Message();
        message.chatId = user.getPhone()+"@c.us";
        message.text = NotificationFormatter.formatShortMessage(userId, event, position).trim();

        byte[] out = Message.class.toString().getBytes(StandardCharsets.UTF_8);

        Context.getClient().target(url).request()
                 .async().post(Entity.json(message), new InvocationCallback<Object>() {
                    //.async().post(Entity.entity(out,MediaType.APPLICATION_JSON_TYPE), new InvocationCallback<Object>() {
            @Override
            public void completed(Object o) {
            }

            @Override
            public void failed(Throwable throwable) {
                LOGGER.warn("WhatsApp API error", throwable);
            }
        });
    }

    @Override
    public void sendAsync(long userId, Event event, Position position) {
        sendSync(userId, event, position);
    }

}