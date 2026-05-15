package com.charmflex.app.flexiexpensesmanager.splitbill.dto

data class CreateSplitGroupRequest(
    val name: String,
    val sourceLocalBillId: Int? = null,
)

data class JoinSplitGroupRequest(
    val inviteCode: String,
)

data class CreateRemoteBillRequest(
    val description: String,
    val totalMinorUnitAmount: Long,
    val currencyCode: String,
    val localTransactionId: Long?,
    val payerRemoteMemberId: String,
    val participants: List<RemoteBillParticipantRequest>,
)

data class RemoteBillParticipantRequest(
    val debtorRemoteMemberId: String,
    val owedMinorUnitAmount: Long,
)

data class CreateRemotePaymentRequest(
    val remoteBillId: String,
    val payerRemoteMemberId: String,
    val receiverRemoteMemberId: String,
    val minorUnitAmount: Long,
    val currencyCode: String,
    val localTransactionId: Long?,
)

data class RegisterNotificationTokenRequest(
    val platform: String,
    val token: String,
)

data class EmptyResponse(
    val success: Boolean = true,
)

data class SplitGroupListResponse(
    val groups: List<SplitGroupResponse>,
)

data class SplitGroupResponse(
    val remoteGroupId: String,
    val name: String,
    val inviteCode: String?,
    val ownerRemoteUserId: String,
    val members: List<SplitGroupMemberResponse>,
    val bills: List<RemoteBillResponse>,
    val payments: List<RemotePaymentResponse>,
    val balances: List<RemoteBalanceResponse>,
    val createdAt: String,
    val updatedAt: String?,
)

data class SplitGroupMemberResponse(
    val remoteMemberId: String,
    val remoteUserId: String,
    val displayName: String,
    val email: String?,
)

data class RemoteBillResponse(
    val remoteBillId: String,
    val remoteGroupId: String,
    val description: String,
    val totalMinorUnitAmount: Long,
    val currencyCode: String,
    val payerRemoteMemberId: String,
    val participants: List<RemoteBillParticipantResponse>,
    val createdAt: String,
    val updatedAt: String?,
)

data class RemoteBillParticipantResponse(
    val remoteParticipantId: String,
    val debtorRemoteMemberId: String,
    val owedMinorUnitAmount: Long,
    val paidMinorUnitAmount: Long,
    val isSettled: Boolean,
)

data class RemotePaymentResponse(
    val remotePaymentId: String,
    val remoteGroupId: String,
    val remoteBillId: String,
    val payerRemoteMemberId: String,
    val receiverRemoteMemberId: String,
    val minorUnitAmount: Long,
    val currencyCode: String,
    val createdAt: String,
)

data class RemoteBalanceResponse(
    val fromRemoteMemberId: String,
    val toRemoteMemberId: String,
    val minorUnitAmount: Long,
    val currencyCode: String,
)
