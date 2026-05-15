package com.charmflex.app.flexiexpensesmanager.splitbill.service

import com.charmflex.app.flexiexpensesmanager.splitbill.model.SplitGroupMemberRecord
import com.charmflex.app.flexiexpensesmanager.splitbill.repository.SplitBillRepository
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Service

@Service
class SplitBillNotificationService(
    private val splitBillRepository: SplitBillRepository,
) {
    fun notifyPaymentReceived(
        receiver: SplitGroupMemberRecord,
        remoteGroupId: String,
        remotePaymentId: String,
        amountText: String,
    ) {
        val tokens = splitBillRepository.listEnabledDeviceTokens(receiver.remoteUserId)
        if (tokens.isEmpty()) return

        tokens.forEach { token ->
            runCatching {
                FirebaseMessaging.getInstance().send(
                    Message.builder()
                        .setToken(token)
                        .setNotification(
                            Notification.builder()
                                .setTitle("Split bill payment")
                                .setBody("A payment of $amountText was recorded for you.")
                                .build()
                        )
                        .putData("remoteGroupId", remoteGroupId)
                        .putData("remotePaymentId", remotePaymentId)
                        .putData("title", "Split bill payment")
                        .putData("body", "A payment of $amountText was recorded for you.")
                        .build()
                )
            }
        }
    }
}
