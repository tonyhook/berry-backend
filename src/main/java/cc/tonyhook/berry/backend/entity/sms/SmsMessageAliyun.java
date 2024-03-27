package cc.tonyhook.berry.backend.entity.sms;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SmsMessageAliyun {

    @JsonProperty(value = "phone_number")
    private String phoneNumber;

    @JsonProperty(value = "send_time")
    private String sendTime;

    private String content;

    @JsonProperty(value = "sign_name")
    private String signName;

    @JsonProperty(value = "dest_code")
    private String destCode;

    @JsonProperty(value = "sequence_id")
    private Long sequenceId;

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSendTime() {
        return this.sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSignName() {
        return this.signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }

    public String getDestCode() {
        return this.destCode;
    }

    public void setDestCode(String destCode) {
        this.destCode = destCode;
    }

    public Long getSequenceId() {
        return this.sequenceId;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }

}
