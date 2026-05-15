package com.charmflex.app.flexiexpensesmanager.splitbill.model

import java.time.OffsetDateTime

data class SplitGroupRecord(
    val remoteGroupId: String,
    val ownerRemoteUserId: String,
    val name: String,
    val inviteCode: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?,
)

data class SplitGroupMemberRecord(
    val remoteMemberId: String,
    val remoteGroupId: String,
    val remoteUserId: String,
    val role: String,
    val displayName: String,
    val email: String?,
)

data class SplitBillRecord(
    val remoteBillId: String,
    val remoteGroupId: String,
    val description: String,
    val totalMinorUnitAmount: Long,
    val currencyCode: String,
    val payerRemoteMemberId: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?,
)

data class SplitBillParticipantRecord(
    val remoteParticipantId: String,
    val remoteBillId: String,
    val debtorRemoteMemberId: String,
    val owedMinorUnitAmount: Long,
    val paidMinorUnitAmount: Long,
    val isSettled: Boolean,
)

data class SplitPaymentRecord(
    val remotePaymentId: String,
    val remoteGroupId: String,
    val remoteBillId: String,
    val payerRemoteMemberId: String,
    val receiverRemoteMemberId: String,
    val minorUnitAmount: Long,
    val currencyCode: String,
    val createdAt: OffsetDateTime,
)
