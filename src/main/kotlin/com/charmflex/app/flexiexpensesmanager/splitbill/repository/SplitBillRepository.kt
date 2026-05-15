package com.charmflex.app.flexiexpensesmanager.splitbill.repository

import com.charmflex.app.flexiexpensesmanager.auth.AuthenticatedUser
import com.charmflex.app.flexiexpensesmanager.splitbill.model.SplitBillParticipantRecord
import com.charmflex.app.flexiexpensesmanager.splitbill.model.SplitBillRecord
import com.charmflex.app.flexiexpensesmanager.splitbill.model.SplitGroupMemberRecord
import com.charmflex.app.flexiexpensesmanager.splitbill.model.SplitGroupRecord
import com.charmflex.app.flexiexpensesmanager.splitbill.model.SplitPaymentRecord
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class SplitBillRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    fun upsertUser(user: AuthenticatedUser) {
        jdbcTemplate.update(
            """
            INSERT INTO app_users(remote_user_id, display_name, email, created_at, updated_at)
            VALUES (:remoteUserId, :displayName, :email, NOW(), NOW())
            ON CONFLICT (remote_user_id) DO UPDATE SET
                display_name = EXCLUDED.display_name,
                email = EXCLUDED.email,
                updated_at = NOW()
            """.trimIndent(),
            mapOf(
                "remoteUserId" to user.remoteUserId,
                "displayName" to user.displayName,
                "email" to user.email
            )
        )
    }

    fun insertGroup(remoteGroupId: String, ownerRemoteUserId: String, name: String) {
        jdbcTemplate.update(
            """
            INSERT INTO split_groups(remote_group_id, owner_user_id, name, created_at, updated_at)
            VALUES (:remoteGroupId, :ownerRemoteUserId, :name, NOW(), NOW())
            """.trimIndent(),
            mapOf(
                "remoteGroupId" to remoteGroupId,
                "ownerRemoteUserId" to ownerRemoteUserId,
                "name" to name
            )
        )
    }

    fun insertMember(
        remoteMemberId: String,
        remoteGroupId: String,
        user: AuthenticatedUser,
        role: String,
    ) {
        jdbcTemplate.update(
            """
            INSERT INTO split_group_members(
                remote_member_id, remote_group_id, remote_user_id, role, display_name, email, joined_at
            )
            VALUES (:remoteMemberId, :remoteGroupId, :remoteUserId, :role, :displayName, :email, NOW())
            ON CONFLICT (remote_group_id, remote_user_id) DO NOTHING
            """.trimIndent(),
            mapOf(
                "remoteMemberId" to remoteMemberId,
                "remoteGroupId" to remoteGroupId,
                "remoteUserId" to user.remoteUserId,
                "role" to role,
                "displayName" to user.displayName,
                "email" to user.email
            )
        )
    }

    fun updateInviteCode(remoteGroupId: String, inviteCode: String) {
        jdbcTemplate.update(
            """
            UPDATE split_groups
            SET invite_code = :inviteCode, updated_at = NOW()
            WHERE remote_group_id = :remoteGroupId
            """.trimIndent(),
            mapOf("remoteGroupId" to remoteGroupId, "inviteCode" to inviteCode)
        )
    }

    fun findGroup(remoteGroupId: String): SplitGroupRecord? {
        return queryGroup(
            "SELECT * FROM split_groups WHERE remote_group_id = :remoteGroupId LIMIT 1",
            mapOf("remoteGroupId" to remoteGroupId)
        ).firstOrNull()
    }

    fun findGroupByInviteCode(inviteCode: String): SplitGroupRecord? {
        return queryGroup(
            "SELECT * FROM split_groups WHERE invite_code = :inviteCode LIMIT 1",
            mapOf("inviteCode" to inviteCode)
        ).firstOrNull()
    }

    fun listGroupsForUser(remoteUserId: String): List<SplitGroupRecord> {
        return queryGroup(
            """
            SELECT g.* FROM split_groups g
            INNER JOIN split_group_members m ON m.remote_group_id = g.remote_group_id
            WHERE m.remote_user_id = :remoteUserId
            ORDER BY g.updated_at DESC
            """.trimIndent(),
            mapOf("remoteUserId" to remoteUserId)
        )
    }

    fun findMember(remoteGroupId: String, remoteUserId: String): SplitGroupMemberRecord? {
        return queryMember(
            """
            SELECT * FROM split_group_members
            WHERE remote_group_id = :remoteGroupId AND remote_user_id = :remoteUserId
            LIMIT 1
            """.trimIndent(),
            mapOf("remoteGroupId" to remoteGroupId, "remoteUserId" to remoteUserId)
        ).firstOrNull()
    }

    fun findMemberById(remoteGroupId: String, remoteMemberId: String): SplitGroupMemberRecord? {
        return queryMember(
            """
            SELECT * FROM split_group_members
            WHERE remote_group_id = :remoteGroupId AND remote_member_id = :remoteMemberId
            LIMIT 1
            """.trimIndent(),
            mapOf("remoteGroupId" to remoteGroupId, "remoteMemberId" to remoteMemberId)
        ).firstOrNull()
    }

    fun listMembers(remoteGroupId: String): List<SplitGroupMemberRecord> {
        return queryMember(
            """
            SELECT * FROM split_group_members
            WHERE remote_group_id = :remoteGroupId
            ORDER BY joined_at ASC
            """.trimIndent(),
            mapOf("remoteGroupId" to remoteGroupId)
        )
    }

    fun insertBill(
        remoteBillId: String,
        remoteGroupId: String,
        description: String,
        totalMinorUnitAmount: Long,
        currencyCode: String,
        payerRemoteMemberId: String,
        creatorRemoteMemberId: String,
    ) {
        jdbcTemplate.update(
            """
            INSERT INTO split_bills(
                remote_bill_id, remote_group_id, description, total_minor_unit_amount,
                currency_code, payer_remote_member_id, creator_remote_member_id, created_at, updated_at
            )
            VALUES (
                :remoteBillId, :remoteGroupId, :description, :totalMinorUnitAmount,
                :currencyCode, :payerRemoteMemberId, :creatorRemoteMemberId, NOW(), NOW()
            )
            """.trimIndent(),
            mapOf(
                "remoteBillId" to remoteBillId,
                "remoteGroupId" to remoteGroupId,
                "description" to description,
                "totalMinorUnitAmount" to totalMinorUnitAmount,
                "currencyCode" to currencyCode,
                "payerRemoteMemberId" to payerRemoteMemberId,
                "creatorRemoteMemberId" to creatorRemoteMemberId
            )
        )
    }

    fun insertParticipants(participants: List<SplitBillParticipantRecord>) {
        if (participants.isEmpty()) return
        val params = participants.map {
            MapSqlParameterSource()
                .addValue("remoteParticipantId", it.remoteParticipantId)
                .addValue("remoteBillId", it.remoteBillId)
                .addValue("debtorRemoteMemberId", it.debtorRemoteMemberId)
                .addValue("owedMinorUnitAmount", it.owedMinorUnitAmount)
                .addValue("paidMinorUnitAmount", it.paidMinorUnitAmount)
                .addValue("isSettled", it.isSettled)
        }.toTypedArray()
        jdbcTemplate.batchUpdate(
            """
            INSERT INTO split_bill_participants(
                remote_participant_id, remote_bill_id, debtor_remote_member_id,
                owed_minor_unit_amount, paid_minor_unit_amount, is_settled
            )
            VALUES (
                :remoteParticipantId, :remoteBillId, :debtorRemoteMemberId,
                :owedMinorUnitAmount, :paidMinorUnitAmount, :isSettled
            )
            """.trimIndent(),
            params
        )
    }

    fun findBill(remoteGroupId: String, remoteBillId: String): SplitBillRecord? {
        return queryBill(
            """
            SELECT * FROM split_bills
            WHERE remote_group_id = :remoteGroupId AND remote_bill_id = :remoteBillId
            LIMIT 1
            """.trimIndent(),
            mapOf("remoteGroupId" to remoteGroupId, "remoteBillId" to remoteBillId)
        ).firstOrNull()
    }

    fun listBills(remoteGroupId: String): List<SplitBillRecord> {
        return queryBill(
            """
            SELECT * FROM split_bills
            WHERE remote_group_id = :remoteGroupId
            ORDER BY created_at DESC
            """.trimIndent(),
            mapOf("remoteGroupId" to remoteGroupId)
        )
    }

    fun listParticipants(remoteGroupId: String): List<SplitBillParticipantRecord> {
        return jdbcTemplate.query(
            """
            SELECT p.* FROM split_bill_participants p
            INNER JOIN split_bills b ON b.remote_bill_id = p.remote_bill_id
            WHERE b.remote_group_id = :remoteGroupId
            ORDER BY b.created_at DESC
            """.trimIndent(),
            mapOf("remoteGroupId" to remoteGroupId)
        ) { rs, _ ->
            SplitBillParticipantRecord(
                remoteParticipantId = rs.getString("remote_participant_id"),
                remoteBillId = rs.getString("remote_bill_id"),
                debtorRemoteMemberId = rs.getString("debtor_remote_member_id"),
                owedMinorUnitAmount = rs.getLong("owed_minor_unit_amount"),
                paidMinorUnitAmount = rs.getLong("paid_minor_unit_amount"),
                isSettled = rs.getBoolean("is_settled")
            )
        }
    }

    fun insertPayment(
        remotePaymentId: String,
        remoteGroupId: String,
        remoteBillId: String,
        payerRemoteMemberId: String,
        receiverRemoteMemberId: String,
        minorUnitAmount: Long,
        currencyCode: String,
        creatorRemoteMemberId: String,
    ) {
        jdbcTemplate.update(
            """
            INSERT INTO split_payments(
                remote_payment_id, remote_group_id, remote_bill_id, payer_remote_member_id,
                receiver_remote_member_id, minor_unit_amount, currency_code, creator_remote_member_id, created_at
            )
            VALUES (
                :remotePaymentId, :remoteGroupId, :remoteBillId, :payerRemoteMemberId,
                :receiverRemoteMemberId, :minorUnitAmount, :currencyCode, :creatorRemoteMemberId, NOW()
            )
            """.trimIndent(),
            mapOf(
                "remotePaymentId" to remotePaymentId,
                "remoteGroupId" to remoteGroupId,
                "remoteBillId" to remoteBillId,
                "payerRemoteMemberId" to payerRemoteMemberId,
                "receiverRemoteMemberId" to receiverRemoteMemberId,
                "minorUnitAmount" to minorUnitAmount,
                "currencyCode" to currencyCode,
                "creatorRemoteMemberId" to creatorRemoteMemberId
            )
        )
    }

    fun applyPaymentToParticipant(remoteBillId: String, debtorRemoteMemberId: String, amount: Long) {
        jdbcTemplate.update(
            """
            UPDATE split_bill_participants
            SET
                paid_minor_unit_amount = LEAST(owed_minor_unit_amount, paid_minor_unit_amount + :amount),
                is_settled = paid_minor_unit_amount + :amount >= owed_minor_unit_amount
            WHERE remote_bill_id = :remoteBillId
                AND debtor_remote_member_id = :debtorRemoteMemberId
            """.trimIndent(),
            mapOf(
                "remoteBillId" to remoteBillId,
                "debtorRemoteMemberId" to debtorRemoteMemberId,
                "amount" to amount
            )
        )
    }

    fun listPayments(remoteGroupId: String): List<SplitPaymentRecord> {
        return jdbcTemplate.query(
            """
            SELECT * FROM split_payments
            WHERE remote_group_id = :remoteGroupId
            ORDER BY created_at DESC
            """.trimIndent(),
            mapOf("remoteGroupId" to remoteGroupId)
        ) { rs, _ ->
            SplitPaymentRecord(
                remotePaymentId = rs.getString("remote_payment_id"),
                remoteGroupId = rs.getString("remote_group_id"),
                remoteBillId = rs.getString("remote_bill_id"),
                payerRemoteMemberId = rs.getString("payer_remote_member_id"),
                receiverRemoteMemberId = rs.getString("receiver_remote_member_id"),
                minorUnitAmount = rs.getLong("minor_unit_amount"),
                currencyCode = rs.getString("currency_code"),
                createdAt = rs.getObject("created_at", OffsetDateTime::class.java)
            )
        }
    }

    fun listEnabledDeviceTokens(remoteUserId: String): List<String> {
        return jdbcTemplate.query(
            """
            SELECT push_token FROM user_devices
            WHERE remote_user_id = :remoteUserId AND enabled = TRUE
            ORDER BY updated_at DESC
            """.trimIndent(),
            mapOf("remoteUserId" to remoteUserId)
        ) { rs, _ -> rs.getString("push_token") }
    }

    fun upsertDevice(remoteUserId: String, platform: String, token: String) {
        jdbcTemplate.update(
            """
            INSERT INTO user_devices(remote_user_id, platform, push_token, enabled, created_at, updated_at, last_seen_at)
            VALUES (:remoteUserId, :platform, :token, TRUE, NOW(), NOW(), NOW())
            ON CONFLICT (push_token) DO UPDATE SET
                remote_user_id = EXCLUDED.remote_user_id,
                platform = EXCLUDED.platform,
                enabled = TRUE,
                updated_at = NOW(),
                last_seen_at = NOW()
            """.trimIndent(),
            mapOf("remoteUserId" to remoteUserId, "platform" to platform, "token" to token)
        )
    }

    private fun queryGroup(sql: String, params: Map<String, Any>): List<SplitGroupRecord> {
        return jdbcTemplate.query(sql, params) { rs, _ ->
            SplitGroupRecord(
                remoteGroupId = rs.getString("remote_group_id"),
                ownerRemoteUserId = rs.getString("owner_user_id"),
                name = rs.getString("name"),
                inviteCode = rs.getString("invite_code"),
                createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java)
            )
        }
    }

    private fun queryMember(sql: String, params: Map<String, Any>): List<SplitGroupMemberRecord> {
        return jdbcTemplate.query(sql, params) { rs, _ ->
            SplitGroupMemberRecord(
                remoteMemberId = rs.getString("remote_member_id"),
                remoteGroupId = rs.getString("remote_group_id"),
                remoteUserId = rs.getString("remote_user_id"),
                role = rs.getString("role"),
                displayName = rs.getString("display_name"),
                email = rs.getString("email")
            )
        }
    }

    private fun queryBill(sql: String, params: Map<String, Any>): List<SplitBillRecord> {
        return jdbcTemplate.query(sql, params) { rs, _ ->
            SplitBillRecord(
                remoteBillId = rs.getString("remote_bill_id"),
                remoteGroupId = rs.getString("remote_group_id"),
                description = rs.getString("description"),
                totalMinorUnitAmount = rs.getLong("total_minor_unit_amount"),
                currencyCode = rs.getString("currency_code"),
                payerRemoteMemberId = rs.getString("payer_remote_member_id"),
                createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java)
            )
        }
    }
}
