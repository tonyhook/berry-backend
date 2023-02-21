package cc.tonyhook.berry.backend.entity.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "wechat_message")
public class WechatMessage {

    @EmbeddedId
    @JsonUnwrapped
    private WechatMessageId id;

    @JsonProperty(value = "ToUserName")
    private String ToUserName;

    @JsonProperty(value = "MsgType")
    private String MsgType;

    @JsonProperty(value = "MsgId")
    private String MsgId;

    // MsgType == "text"
    @JsonProperty(value = "Content")
    private String Content;

    // MsgType == "image" || "voice" || "shortvideo"
    @JsonProperty(value = "MediaId")
    private String MediaId;

    // MsgType == "image"
    @JsonProperty(value = "PicUrl")
    private String PicUrl;

    // MsgType == "voice"
    @JsonProperty(value = "Format")
    private String Format;

    // MsgType == "voice"
    @JsonProperty(value = "Recognition")
    private String Recognition;

    // MsgType == "shortvideo"
    @JsonProperty(value = "ThumbMediaId")
    private String ThumbMediaId;

    // MsgType == "location"
    @JsonProperty(value = "Location_X")
    private String Location_X;

    // MsgType == "location"
    @JsonProperty(value = "Location_Y")
    private String Location_Y;

    // MsgType == "location"
    @JsonProperty(value = "Scale")
    private String Scale;

    // MsgType == "location"
    @JsonProperty(value = "Label")
    private String Label;

    // MsgType == "event"
    @JsonProperty(value = "Event")
    private String Event;

    // MsgType == "event" && (Event == "subscribe" || Event == "SCAN" || Event == "CLICK")
    @JsonProperty(value = "EventKey")
    private String EventKey;

    // MsgType == "event" && (Event == "subscribe" || Event == "SCAN")
    @JsonProperty(value = "Ticket")
    private String Ticket;

    // MsgType == "event" && Event == "LOCATION"
    @JsonProperty(value = "Latitude")
    private String Latitude;

    // MsgType == "event" && Event == "LOCATION"
    @JsonProperty(value = "Longitude")
    private String Longitude;

    // MsgType == "event" && Event == "LOCATION"
    @Column(name = "precision1")
    @JsonProperty(value = "Precision")
    private String Precision;

    // MsgType == "event" && Event == "TEMPLATESENDJOBFINISH"
    @JsonProperty(value = "Status")
    private String Status;

    public WechatMessageId getId() {
        return this.id;
    }

    public void setId(WechatMessageId id) {
        this.id = id;
    }

    public String getToUserName() {
        return this.ToUserName;
    }

    public void setToUserName(String ToUserName) {
        this.ToUserName = ToUserName;
    }

    public String getMsgType() {
        return this.MsgType;
    }

    public void setMsgType(String MsgType) {
        this.MsgType = MsgType;
    }

    public String getMsgId() {
        return this.MsgId;
    }

    public void setMsgId(String MsgId) {
        this.MsgId = MsgId;
    }

    public String getContent() {
        return this.Content;
    }

    public void setContent(String Content) {
        this.Content = Content;
    }

    public String getMediaId() {
        return this.MediaId;
    }

    public void setMediaId(String MediaId) {
        this.MediaId = MediaId;
    }

    public String getPicUrl() {
        return this.PicUrl;
    }

    public void setPicUrl(String PicUrl) {
        this.PicUrl = PicUrl;
    }

    public String getFormat() {
        return this.Format;
    }

    public void setFormat(String Format) {
        this.Format = Format;
    }

    public String getRecognition() {
        return this.Recognition;
    }

    public void setRecognition(String Recognition) {
        this.Recognition = Recognition;
    }

    public String getThumbMediaId() {
        return this.ThumbMediaId;
    }

    public void setThumbMediaId(String ThumbMediaId) {
        this.ThumbMediaId = ThumbMediaId;
    }

    public String getLocation_X() {
        return this.Location_X;
    }

    public void setLocation_X(String Location_X) {
        this.Location_X = Location_X;
    }

    public String getLocation_Y() {
        return this.Location_Y;
    }

    public void setLocation_Y(String Location_Y) {
        this.Location_Y = Location_Y;
    }

    public String getScale() {
        return this.Scale;
    }

    public void setScale(String Scale) {
        this.Scale = Scale;
    }

    public String getLabel() {
        return this.Label;
    }

    public void setLabel(String Label) {
        this.Label = Label;
    }

    public String getEvent() {
        return this.Event;
    }

    public void setEvent(String Event) {
        this.Event = Event;
    }

    public String getEventKey() {
        return this.EventKey;
    }

    public void setEventKey(String EventKey) {
        this.EventKey = EventKey;
    }

    public String getTicket() {
        return this.Ticket;
    }

    public void setTicket(String Ticket) {
        this.Ticket = Ticket;
    }

    public String getLatitude() {
        return this.Latitude;
    }

    public void setLatitude(String Latitude) {
        this.Latitude = Latitude;
    }

    public String getLongitude() {
        return this.Longitude;
    }

    public void setLongitude(String Longitude) {
        this.Longitude = Longitude;
    }

    public String getPrecision() {
        return this.Precision;
    }

    public void setPrecision(String Precision) {
        this.Precision = Precision;
    }

    public String getStatus() {
        return this.Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }

}
