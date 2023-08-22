package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.TikTokEventBuilder;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventHandler;
import io.github.jwdeveloper.tiktok.events.messages.*;
import io.github.jwdeveloper.tiktok.handlers.WebResponseHandler;
import io.github.jwdeveloper.tiktok.http.TikTokApiService;
import io.github.jwdeveloper.tiktok.http.TikTokCookieJar;
import io.github.jwdeveloper.tiktok.http.TikTokHttpApiClient;
import io.github.jwdeveloper.tiktok.http.TikTokHttpRequestFactory;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.TikTokRoomInfo;
import io.github.jwdeveloper.tiktok.websocket.TikTokWebSocketClient;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class TikTokLiveClientBuilder implements TikTokEventBuilder<TikTokLiveClientBuilder> {
    private final ClientSettings clientSettings;
    private final Logger logger;
    private final TikTokEventHandler tikTokEventHandler;

    public TikTokLiveClientBuilder(String userName) {
        this.tikTokEventHandler = new TikTokEventHandler();
        this.clientSettings = Constants.DefaultClientSettings();
        this.clientSettings.setHostName(userName);
        this.logger = Logger.getLogger(TikTokLive.class.getName());
    }

    public TikTokLiveClientBuilder configure(Consumer<ClientSettings> consumer) {
        consumer.accept(clientSettings);
        return this;
    }

    private void validate() {

        if (clientSettings.getTimeout() == null) {
            clientSettings.setTimeout(Duration.ofSeconds(Constants.DEFAULT_TIMEOUT));
        }

        if (clientSettings.getPollingInterval() == null) {
            clientSettings.setPollingInterval(Duration.ofSeconds(Constants.DEFAULT_POLLTIME));
        }

        if (clientSettings.getClientLanguage() == null || clientSettings.getClientLanguage().equals("")) {
            clientSettings.setClientLanguage(Constants.DefaultClientSettings().getClientLanguage());
        }

        if (clientSettings.getSocketBufferSize() < 500_000) {
            clientSettings.setSocketBufferSize(Constants.DefaultClientSettings().getSocketBufferSize());
        }


        if (clientSettings.getHostName() == null || clientSettings.getHostName() .equals("")) {
            throw new RuntimeException("HostName can not be null");
        }

        var params = clientSettings.getClientParameters();
        params.put("app_language", clientSettings.getClientLanguage());
        params.put("webcast_language", clientSettings.getClientLanguage());
    }

    public LiveClient build() {
        validate();


        var meta = new TikTokRoomInfo();
        meta.setUserName(clientSettings.getHostName());


        var cookieJar = new TikTokCookieJar();
        var requestFactory = new TikTokHttpRequestFactory(cookieJar);
        var apiClient = new TikTokHttpApiClient(cookieJar, requestFactory);
        var apiService = new TikTokApiService(apiClient, logger, clientSettings);
        var giftManager = new TikTokGiftManager(logger, apiService, clientSettings);
        var webResponseHandler = new WebResponseHandler(tikTokEventHandler,giftManager);
        var webSocketClient = new TikTokWebSocketClient(logger,
                cookieJar,
                requestFactory,
                clientSettings,
                webResponseHandler,
                tikTokEventHandler);

        return new TikTokLiveClient(meta, apiService, webSocketClient, giftManager, tikTokEventHandler, logger);
    }

    public LiveClient buildAndRun() {
        var client = build();
        client.connect();
        return client;
    }

    public TikTokLiveClientBuilder onUnhandledSocial(Consumer<TikTokUnhandledSocialEvent> event) {
        tikTokEventHandler.subscribe(TikTokUnhandledSocialEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMicFanTicket(Consumer<TikTokLinkMicFanTicketEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkMicFanTicketEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onEnvelope(Consumer<TikTokEnvelopeEvent> event) {
        tikTokEventHandler.subscribe(TikTokEnvelopeEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onShopMessage(Consumer<TikTokShopMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokShopMessageEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onDetectMessage(Consumer<TikTokDetectMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokDetectMessageEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkLayerMessage(Consumer<TikTokLinkLayerMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkLayerMessageEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onConnected(Consumer<TikTokConnectedEvent> event) {
        tikTokEventHandler.subscribe(TikTokConnectedEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onCaption(Consumer<TikTokCaptionEvent> event) {
        tikTokEventHandler.subscribe(TikTokCaptionEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onQuestion(Consumer<TikTokQuestionEvent> event) {
        tikTokEventHandler.subscribe(TikTokQuestionEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onRoomPinMessage(Consumer<TikTokRoomPinMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokRoomPinMessageEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onRoomMessage(Consumer<TikTokRoomMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokRoomMessageEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onLivePaused(Consumer<TikTokLivePausedEvent> event) {
        tikTokEventHandler.subscribe(TikTokLivePausedEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onLike(Consumer<TikTokLikeEvent> event) {
        tikTokEventHandler.subscribe(TikTokLikeEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMessage(Consumer<TikTokLinkMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkMessageEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onBarrageMessage(Consumer<TikTokBarrageMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokBarrageMessageEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onGiftMessage(Consumer<TikTokGiftMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokGiftMessageEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMicArmies(Consumer<TikTokLinkMicArmiesEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkMicArmiesEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onEmote(Consumer<TikTokEmoteEvent> event) {
        tikTokEventHandler.subscribe(TikTokEmoteEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onUnauthorizedMember(
            Consumer<TikTokUnauthorizedMemberEvent> event) {
        tikTokEventHandler.subscribe(TikTokUnauthorizedMemberEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onInRoomBanner(Consumer<TikTokInRoomBannerEvent> event) {
        tikTokEventHandler.subscribe(TikTokInRoomBannerEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMicMethod(Consumer<TikTokLinkMicMethodEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkMicMethodEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onSubscribe(Consumer<TikTokSubscribeEvent> event) {
        tikTokEventHandler.subscribe(TikTokSubscribeEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onPollMessage(Consumer<TikTokPollMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokPollMessageEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onFollow(Consumer<TikTokFollowEvent> event) {
        tikTokEventHandler.subscribe(TikTokFollowEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onRoomViewerData(Consumer<TikTokRoomViewerDataEvent> event) {
        tikTokEventHandler.subscribe(TikTokRoomViewerDataEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onGoalUpdate(Consumer<TikTokGoalUpdateEvent> event) {
        tikTokEventHandler.subscribe(TikTokGoalUpdateEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onComment(Consumer<TikTokCommentEvent> event) {
        tikTokEventHandler.subscribe(TikTokCommentEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onRankUpdate(Consumer<TikTokRankUpdateEvent> event) {
        tikTokEventHandler.subscribe(TikTokRankUpdateEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onIMDelete(Consumer<TikTokIMDeleteEvent> event) {
        tikTokEventHandler.subscribe(TikTokIMDeleteEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onLiveEnded(Consumer<TikTokLiveEndedEvent> event) {
        tikTokEventHandler.subscribe(TikTokLiveEndedEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onError(Consumer<TikTokErrorEvent> event) {
        tikTokEventHandler.subscribe(TikTokErrorEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onUnhandled(Consumer<TikTokUnhandledEvent> event) {
        tikTokEventHandler.subscribe(TikTokUnhandledEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onJoin(Consumer<TikTokJoinEvent> event) {
        tikTokEventHandler.subscribe(TikTokJoinEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onRankText(Consumer<TikTokRankTextEvent> event) {
        tikTokEventHandler.subscribe(TikTokRankTextEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onShare(Consumer<TikTokShareEvent> event) {
        tikTokEventHandler.subscribe(TikTokShareEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onUnhandledMember(Consumer<TikTokUnhandledMemberEvent> event) {
        tikTokEventHandler.subscribe(TikTokUnhandledMemberEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onSubNotify(Consumer<TikTokSubNotifyEvent> event) {
        tikTokEventHandler.subscribe(TikTokSubNotifyEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMicBattle(Consumer<TikTokLinkMicBattleEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkMicBattleEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onDisconnected(Consumer<TikTokDisconnectedEvent> event) {
        tikTokEventHandler.subscribe(TikTokDisconnectedEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onGiftBroadcast(Consumer<TikTokGiftBroadcastEvent> event) {
        tikTokEventHandler.subscribe(TikTokGiftBroadcastEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onUnhandledControl(Consumer<TikTokUnhandledControlEvent> event) {
        tikTokEventHandler.subscribe(TikTokUnhandledControlEvent.class,event);
        return this;
    }

    public TikTokLiveClientBuilder onEvent(Consumer<TikTokEvent> event) {
        tikTokEventHandler.subscribe(TikTokEvent.class,event);
        return this;
    }
}




