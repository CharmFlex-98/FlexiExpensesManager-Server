package com.charmflex.app.flexiexpensesmanager.splitbill.service

import com.charmflex.app.flexiexpensesmanager.auth.AuthenticatedUser
import com.charmflex.app.flexiexpensesmanager.splitbill.InvalidInviteCodeException
import com.charmflex.app.flexiexpensesmanager.splitbill.InviteCodeRequiredException
import com.charmflex.app.flexiexpensesmanager.splitbill.SplitBillNotFoundException
import com.charmflex.app.flexiexpensesmanager.splitbill.SplitBillParticipantTotalException
import com.charmflex.app.flexiexpensesmanager.splitbill.SplitBillRequestInvalidException
import com.charmflex.app.flexiexpensesmanager.splitbill.SplitGroupForbiddenException
import com.charmflex.app.flexiexpensesmanager.splitbill.SplitGroupNameRequiredException
import com.charmflex.app.flexiexpensesmanager.splitbill.SplitGroupNotFoundException
import com.charmflex.app.flexiexpensesmanager.splitbill.SplitMemberNotFoundException
import com.charmflex.app.flexiexpensesmanager.splitbill.SplitPaymentRequestInvalidException
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.CreateRemoteBillRequest
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.CreateRemotePaymentRequest
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.CreateSplitGroupRequest
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.EmptyResponse
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.JoinSplitGroupRequest
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.RegisterNotificationTokenRequest
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.RemoteBalanceResponse
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.RemoteBillParticipantResponse
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.RemoteBillResponse
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.RemotePaymentResponse
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.SplitGroupListResponse
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.SplitGroupMemberResponse
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.SplitGroupResponse
import com.charmflex.app.flexiexpensesmanager.splitbill.model.SplitBillParticipantRecord
import com.charmflex.app.flexiexpensesmanager.splitbill.model.SplitBillRecord
import com.charmflex.app.flexiexpensesmanager.splitbill.model.SplitGroupMemberRecord
import com.charmflex.app.flexiexpensesmanager.splitbill.model.SplitGroupRecord
import com.charmflex.app.flexiexpensesmanager.splitbill.model.SplitPaymentRecord
import com.charmflex.app.flexiexpensesmanager.splitbill.repository.SplitBillRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID

@Service
class SplitBillService(
    private val splitBillRepository: SplitBillRepository,
    private val splitBillNotificationService: SplitBillNotificationService,
) {
    private val secureRandom = SecureRandom()
    private val inviteAlphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray()

    @Transactional
    fun createGroup(user: AuthenticatedUser, request: CreateSplitGroupRequest): SplitGroupResponse {
        if (request.name.isBlank()) throw SplitGroupNameRequiredException
        splitBillRepository.upsertUser(user)
        val remoteGroupId = newRemoteId()
        splitBillRepository.insertGroup(remoteGroupId, user.remoteUserId, request.name.trim())
        splitBillRepository.insertMember(newRemoteId(), remoteGroupId, user, OWNER_ROLE)
        return getGroup(user, remoteGroupId)
    }

    @Transactional
    fun createInviteCode(user: AuthenticatedUser, remoteGroupId: String): SplitGroupResponse {
        splitBillRepository.upsertUser(user)
        requireMember(remoteGroupId, user.remoteUserId)
        val inviteCode = generateInviteCode()
        splitBillRepository.updateInviteCode(remoteGroupId, inviteCode)
        return getGroup(user, remoteGroupId)
    }

    @Transactional
    fun joinGroup(user: AuthenticatedUser, request: JoinSplitGroupRequest): SplitGroupResponse {
        val inviteCode = request.inviteCode.trim().uppercase(Locale.US)
        if (inviteCode.isBlank()) throw InviteCodeRequiredException
        splitBillRepository.upsertUser(user)
        val group = splitBillRepository.findGroupByInviteCode(inviteCode) ?: throw InvalidInviteCodeException
        splitBillRepository.insertMember(newRemoteId(), group.remoteGroupId, user, MEMBER_ROLE)
        return getGroup(user, group.remoteGroupId)
    }

    @Transactional
    fun listGroups(user: AuthenticatedUser): SplitGroupListResponse {
        splitBillRepository.upsertUser(user)
        val groups = splitBillRepository.listGroupsForUser(user.remoteUserId)
            .map { buildGroupResponse(it) }
        return SplitGroupListResponse(groups)
    }

    @Transactional
    fun getGroup(user: AuthenticatedUser, remoteGroupId: String): SplitGroupResponse {
        splitBillRepository.upsertUser(user)
        requireMember(remoteGroupId, user.remoteUserId)
        val group = splitBillRepository.findGroup(remoteGroupId) ?: throw SplitGroupNotFoundException
        return buildGroupResponse(group)
    }

    @Transactional
    fun createBill(
        user: AuthenticatedUser,
        remoteGroupId: String,
        request: CreateRemoteBillRequest,
    ): RemoteBillResponse {
        splitBillRepository.upsertUser(user)
        val creator = requireMember(remoteGroupId, user.remoteUserId)
        validateBillRequest(remoteGroupId, request)
        splitBillRepository.findMemberById(remoteGroupId, request.payerRemoteMemberId)
            ?: throw SplitMemberNotFoundException
        request.participants.forEach {
            splitBillRepository.findMemberById(remoteGroupId, it.debtorRemoteMemberId)
                ?: throw SplitMemberNotFoundException
        }

        val remoteBillId = newRemoteId()
        splitBillRepository.insertBill(
            remoteBillId = remoteBillId,
            remoteGroupId = remoteGroupId,
            description = request.description.trim(),
            totalMinorUnitAmount = request.totalMinorUnitAmount,
            currencyCode = request.currencyCode.trim().uppercase(Locale.US),
            payerRemoteMemberId = request.payerRemoteMemberId,
            creatorRemoteMemberId = creator.remoteMemberId
        )
        val participants = request.participants.map {
            SplitBillParticipantRecord(
                remoteParticipantId = newRemoteId(),
                remoteBillId = remoteBillId,
                debtorRemoteMemberId = it.debtorRemoteMemberId,
                owedMinorUnitAmount = it.owedMinorUnitAmount,
                paidMinorUnitAmount = 0,
                isSettled = false
            )
        }
        splitBillRepository.insertParticipants(participants)
        return billToResponse(
            splitBillRepository.findBill(remoteGroupId, remoteBillId) ?: throw SplitBillNotFoundException,
            participants
        )
    }

    @Transactional
    fun createPayment(
        user: AuthenticatedUser,
        remoteGroupId: String,
        request: CreateRemotePaymentRequest,
    ): RemotePaymentResponse {
        splitBillRepository.upsertUser(user)
        val creator = requireMember(remoteGroupId, user.remoteUserId)
        validatePaymentRequest(remoteGroupId, request)
        val bill = splitBillRepository.findBill(remoteGroupId, request.remoteBillId) ?: throw SplitBillNotFoundException
        if (!bill.currencyCode.equals(request.currencyCode.trim(), ignoreCase = true)) {
            throw SplitPaymentRequestInvalidException
        }
        splitBillRepository.findMemberById(remoteGroupId, request.payerRemoteMemberId)
            ?: throw SplitMemberNotFoundException
        val receiver = splitBillRepository.findMemberById(remoteGroupId, request.receiverRemoteMemberId)
            ?: throw SplitMemberNotFoundException

        val remotePaymentId = newRemoteId()
        val currencyCode = request.currencyCode.trim().uppercase(Locale.US)
        splitBillRepository.insertPayment(
            remotePaymentId = remotePaymentId,
            remoteGroupId = remoteGroupId,
            remoteBillId = request.remoteBillId,
            payerRemoteMemberId = request.payerRemoteMemberId,
            receiverRemoteMemberId = request.receiverRemoteMemberId,
            minorUnitAmount = request.minorUnitAmount,
            currencyCode = currencyCode,
            creatorRemoteMemberId = creator.remoteMemberId
        )
        if (bill.payerRemoteMemberId == request.receiverRemoteMemberId) {
            splitBillRepository.applyPaymentToParticipant(
                remoteBillId = request.remoteBillId,
                debtorRemoteMemberId = request.payerRemoteMemberId,
                amount = request.minorUnitAmount
            )
        }
        splitBillNotificationService.notifyPaymentReceived(
            receiver = receiver,
            remoteGroupId = remoteGroupId,
            remotePaymentId = remotePaymentId,
            amountText = "$currencyCode ${request.minorUnitAmount}"
        )
        return splitBillRepository.listPayments(remoteGroupId)
            .first { it.remotePaymentId == remotePaymentId }
            .toResponse()
    }

    @Transactional
    fun registerNotificationToken(
        user: AuthenticatedUser,
        request: RegisterNotificationTokenRequest,
    ): EmptyResponse {
        if (request.platform.isBlank() || request.token.isBlank()) throw SplitPaymentRequestInvalidException
        splitBillRepository.upsertUser(user)
        splitBillRepository.upsertDevice(
            remoteUserId = user.remoteUserId,
            platform = request.platform.trim().uppercase(Locale.US),
            token = request.token.trim()
        )
        return EmptyResponse(success = true)
    }

    private fun buildGroupResponse(group: SplitGroupRecord): SplitGroupResponse {
        val members = splitBillRepository.listMembers(group.remoteGroupId)
        val bills = splitBillRepository.listBills(group.remoteGroupId)
        val participants = splitBillRepository.listParticipants(group.remoteGroupId)
        val participantsByBill = participants.groupBy { it.remoteBillId }
        val payments = splitBillRepository.listPayments(group.remoteGroupId)
        return SplitGroupResponse(
            remoteGroupId = group.remoteGroupId,
            name = group.name,
            inviteCode = group.inviteCode,
            ownerRemoteUserId = group.ownerRemoteUserId,
            members = members.map { it.toResponse() },
            bills = bills.map { billToResponse(it, participantsByBill[it.remoteBillId].orEmpty()) },
            payments = payments.map { it.toResponse() },
            balances = computeBalances(bills, participantsByBill, payments),
            createdAt = group.createdAt.toString(),
            updatedAt = group.updatedAt?.toString()
        )
    }

    private fun requireMember(remoteGroupId: String, remoteUserId: String): SplitGroupMemberRecord {
        splitBillRepository.findGroup(remoteGroupId) ?: throw SplitGroupNotFoundException
        return splitBillRepository.findMember(remoteGroupId, remoteUserId) ?: throw SplitGroupForbiddenException
    }

    private fun validateBillRequest(remoteGroupId: String, request: CreateRemoteBillRequest) {
        if (
            remoteGroupId.isBlank() ||
            request.description.isBlank() ||
            request.totalMinorUnitAmount <= 0 ||
            request.currencyCode.isBlank() ||
            request.payerRemoteMemberId.isBlank() ||
            request.participants.isEmpty()
        ) {
            throw SplitBillRequestInvalidException
        }
        request.participants.forEach {
            if (
                it.debtorRemoteMemberId.isBlank() ||
                it.debtorRemoteMemberId == request.payerRemoteMemberId ||
                it.owedMinorUnitAmount <= 0
            ) {
                throw SplitBillRequestInvalidException
            }
        }
        val participantTotal = try {
            request.participants.fold(0L) { total, participant ->
                Math.addExact(total, participant.owedMinorUnitAmount)
            }
        } catch (_: ArithmeticException) {
            throw SplitBillParticipantTotalException
        }
        if (participantTotal != request.totalMinorUnitAmount) {
            throw SplitBillParticipantTotalException
        }
    }

    private fun validatePaymentRequest(remoteGroupId: String, request: CreateRemotePaymentRequest) {
        if (
            remoteGroupId.isBlank() ||
            request.remoteBillId.isBlank() ||
            request.payerRemoteMemberId.isBlank() ||
            request.receiverRemoteMemberId.isBlank() ||
            request.minorUnitAmount <= 0 ||
            request.currencyCode.isBlank()
        ) {
            throw SplitPaymentRequestInvalidException
        }
    }

    private fun computeBalances(
        bills: List<SplitBillRecord>,
        participantsByBill: Map<String, List<SplitBillParticipantRecord>>,
        payments: List<SplitPaymentRecord>,
    ): List<RemoteBalanceResponse> {
        val deltas = mutableMapOf<BalanceKey, Long>()
        bills.forEach { bill ->
            participantsByBill[bill.remoteBillId].orEmpty().forEach { participant ->
                val key = BalanceKey(participant.debtorRemoteMemberId, bill.payerRemoteMemberId, bill.currencyCode)
                deltas[key] = (deltas[key] ?: 0) + participant.owedMinorUnitAmount
            }
        }
        payments.forEach { payment ->
            val key = BalanceKey(payment.payerRemoteMemberId, payment.receiverRemoteMemberId, payment.currencyCode)
            deltas[key] = (deltas[key] ?: 0) - payment.minorUnitAmount
        }
        return deltas
            .filter { it.value != 0L }
            .map { (key, amount) ->
                if (amount > 0) {
                    RemoteBalanceResponse(key.fromRemoteMemberId, key.toRemoteMemberId, amount, key.currencyCode)
                } else {
                    RemoteBalanceResponse(key.toRemoteMemberId, key.fromRemoteMemberId, -amount, key.currencyCode)
                }
            }
            .sortedWith(compareBy({ it.currencyCode }, { it.fromRemoteMemberId }, { it.toRemoteMemberId }))
    }

    private fun generateInviteCode(): String {
        repeat(10) {
            val candidate = buildString {
                repeat(8) {
                    append(inviteAlphabet[secureRandom.nextInt(inviteAlphabet.size)])
                }
            }
            if (splitBillRepository.findGroupByInviteCode(candidate) == null) return candidate
        }
        throw SplitBillRequestInvalidException
    }

    private fun newRemoteId(): String = UUID.randomUUID().toString()

    private fun SplitGroupMemberRecord.toResponse() = SplitGroupMemberResponse(
        remoteMemberId = remoteMemberId,
        remoteUserId = remoteUserId,
        displayName = displayName,
        email = email
    )

    private fun billToResponse(
        bill: SplitBillRecord,
        participants: List<SplitBillParticipantRecord>,
    ) = RemoteBillResponse(
        remoteBillId = bill.remoteBillId,
        remoteGroupId = bill.remoteGroupId,
        description = bill.description,
        totalMinorUnitAmount = bill.totalMinorUnitAmount,
        currencyCode = bill.currencyCode,
        payerRemoteMemberId = bill.payerRemoteMemberId,
        participants = participants.map { it.toResponse() },
        createdAt = bill.createdAt.toString(),
        updatedAt = bill.updatedAt?.toString()
    )

    private fun SplitBillParticipantRecord.toResponse() = RemoteBillParticipantResponse(
        remoteParticipantId = remoteParticipantId,
        debtorRemoteMemberId = debtorRemoteMemberId,
        owedMinorUnitAmount = owedMinorUnitAmount,
        paidMinorUnitAmount = paidMinorUnitAmount,
        isSettled = isSettled
    )

    private fun SplitPaymentRecord.toResponse() = RemotePaymentResponse(
        remotePaymentId = remotePaymentId,
        remoteGroupId = remoteGroupId,
        remoteBillId = remoteBillId,
        payerRemoteMemberId = payerRemoteMemberId,
        receiverRemoteMemberId = receiverRemoteMemberId,
        minorUnitAmount = minorUnitAmount,
        currencyCode = currencyCode,
        createdAt = createdAt.toString()
    )

    private data class BalanceKey(
        val fromRemoteMemberId: String,
        val toRemoteMemberId: String,
        val currencyCode: String,
    )

    companion object {
        private const val OWNER_ROLE = "OWNER"
        private const val MEMBER_ROLE = "MEMBER"
    }
}
