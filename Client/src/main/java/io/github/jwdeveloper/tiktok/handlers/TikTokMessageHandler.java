/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.jwdeveloper.tiktok.handlers;


import com.google.protobuf.ByteString;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.events.messages.websocket.TikTokWebsocketMessageEvent;
import io.github.jwdeveloper.tiktok.events.messages.websocket.TikTokWebsocketResponseEvent;
import io.github.jwdeveloper.tiktok.events.messages.websocket.TikTokWebsocketUnhandledMessageEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveMessageException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokMessageMappingException;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public abstract class TikTokMessageHandler {

    private final Map<String, io.github.jwdeveloper.tiktok.handler.TikTokMessageHandler> handlers;
    private final TikTokEventObserver tikTokEventHandler;

    public TikTokMessageHandler(TikTokEventObserver tikTokEventHandler) {
        handlers = new HashMap<>();
        this.tikTokEventHandler = tikTokEventHandler;
        init();
    }

    public abstract void init();

    public void registerMapping(Class<?> clazz, Function<byte[], TikTokEvent> func) {
        handlers.put(clazz.getSimpleName(), func::apply);
    }

    public void registerMapping(Class<?> input, Class<?> output) {
        registerMapping(input, (e) -> mapMessageToEvent(input, output, e));
    }

    public void handle(LiveClient client, WebcastResponse webcastResponse) {
        tikTokEventHandler.publish(client, new TikTokWebsocketResponseEvent(webcastResponse));
        for (var message : webcastResponse.getMessagesList()) {
            try {
                handleSingleMessage(client, message);
            } catch (Exception e) {
                var exception = new TikTokLiveMessageException(message, webcastResponse, e);
                tikTokEventHandler.publish(client, new TikTokErrorEvent(exception));
            }
        }
    }
    public void handleSingleMessage(LiveClient client, String type, byte[] bytes) throws Exception {

        if (!handlers.containsKey(type)) {
            tikTokEventHandler.publish(client, new TikTokWebsocketUnhandledMessageEvent(WebcastResponse.Message.newBuilder().setMethod(type).build()));
            return;
        }
        var handler = handlers.get(type);
        var tiktokEvent = handler.handle(bytes);
        tikTokEventHandler.publish(client, new TikTokWebsocketMessageEvent(tiktokEvent, WebcastResponse.Message.newBuilder().build()));
        tikTokEventHandler.publish(client, tiktokEvent);
    }


    public void handleSingleMessage(LiveClient client, WebcastResponse.Message message) throws Exception {
        var methodName = message.getMethod();
        if (!methodName.contains("Webcast")) {
            methodName = "Webcast" + methodName;
        }

        if (!handlers.containsKey(methodName)) {
            tikTokEventHandler.publish(client, new TikTokWebsocketUnhandledMessageEvent(message));
            return;
        }
        var handler = handlers.get(methodName);
        var tiktokEvent = handler.handle(message.getPayload().toByteArray());
        tikTokEventHandler.publish(client, new TikTokWebsocketMessageEvent(tiktokEvent, message));
        tikTokEventHandler.publish(client, tiktokEvent);
    }

    protected TikTokEvent mapMessageToEvent(Class<?> inputClazz, Class<?> outputClass, byte[] payload) {
        try {
            var parseMethod = inputClazz.getDeclaredMethod("parseFrom",  byte[].class);
            var deserialized = parseMethod.invoke(null,payload);
            var constructors = Arrays.stream(outputClass.getConstructors())
                    .filter(ea -> Arrays.stream(ea.getParameterTypes())
                            .toList()
                            .contains(inputClazz))
                    .findFirst();

            if (constructors.isEmpty()) {
                throw new TikTokMessageMappingException(inputClazz, outputClass, "Unable to find constructor with input class type");
            }

            var tiktokEvent = constructors.get().newInstance(deserialized);
            return (TikTokEvent) tiktokEvent;
        } catch (Exception ex) {
            throw new TikTokMessageMappingException(inputClazz, outputClass, ex);
        }
    }
}
