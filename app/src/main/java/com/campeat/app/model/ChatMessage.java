package com.campeat.app.model;

public class ChatMessage {

    // ================================
    // TYPE CONSTANTS
    // ================================
    public static final int TYPE_BOT        = 0;
    public static final int TYPE_USER       = 1;
    public static final int TYPE_ORDER_CARD = 2;

    // ================================
    // FIELDS
    // ================================
    private String message;
    private int    type;
    private String time;

    // Untuk TYPE_ORDER_CARD
    private String orderId;
    private String status;
    private String foodName;
    private String estimatedTime;
    private String imageBase64;

    // ================================
    // CONSTRUCTOR — text message
    // ================================
    public ChatMessage(String message, int type, String time) {
        this.message = message;
        this.type    = type;
        this.time    = time;
    }

    // ================================
    // CONSTRUCTOR — order card
    // ================================
    public ChatMessage(
            String orderId,
            String status,
            String foodName,
            String estimatedTime,
            String imageBase64,
            String time
    ) {
        this.type          = TYPE_ORDER_CARD;
        this.orderId       = orderId;
        this.status        = status;
        this.foodName      = foodName;
        this.estimatedTime = estimatedTime;
        this.imageBase64   = imageBase64;
        this.time          = time;
    }

    // ================================
    // GETTERS
    // ================================
    public String getMessage()       { return message;       }
    public int    getType()          { return type;          }
    public String getTime()          { return time;          }
    public String getOrderId()       { return orderId;       }
    public String getStatus()        { return status;        }
    public String getFoodName()      { return foodName;      }
    public String getEstimatedTime() { return estimatedTime; }
    public String getImageBase64()   { return imageBase64;   }
}