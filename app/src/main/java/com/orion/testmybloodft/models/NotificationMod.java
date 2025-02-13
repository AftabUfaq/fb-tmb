package com.orion.testmybloodft.models;

import java.io.Serializable;

/**
 * Created by Wekancode on 30-Mar-17.
 */

/**
 * Model class for notification
 */

public class NotificationMod implements Serializable {
    private int id, to_user_id, order_id, type, status;
    public int from_user_id;
    private String content, created_at, updated_at;

    public NotificationMod(int from_user_id) {
        this.from_user_id = from_user_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFrom_user_id() {
        return from_user_id;
    }

    public void setFrom_user_id(int from_user_id) {
        this.from_user_id = from_user_id;
    }

    public int getTo_user_id() {
        return to_user_id;
    }

    public void setTo_user_id(int to_user_id) {
        this.to_user_id = to_user_id;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "NotificationMod{" +
                "id=" + id +
                ", from_user_id=" + from_user_id +
                ", to_user_id=" + to_user_id +
                ", order_id=" + order_id +
                ", type=" + type +
                ", status=" + status +
                ", content='" + content + '\'' +
                ", created_at='" + created_at + '\'' +
                ", updated_at='" + updated_at + '\'' +
                '}';
    }
}
