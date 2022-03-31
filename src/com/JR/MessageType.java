package com.JR;

public enum MessageType {
        NAME_REQUEST    // запрос имени клиента сервером (к серверу)
        ,USER_NAME      // имя пользователя для сервера
        ,NAME_ACCEPTED  // имя принято сервером
        ,TEXT           //текстовое сообщение
        ,USER_ADDED    //пользователь добавлен
        ,USER_REMOVED   //пользователь удален
}
