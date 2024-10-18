package com.reservation.ticket.concert.domain.dto

data class SeatDTO(
    val id: Long,              // 좌석의 고유 ID
    val seatNumber: String,     // 좌석 번호
    val isAvailable: Boolean    // 좌석 예약 가능 여부
)