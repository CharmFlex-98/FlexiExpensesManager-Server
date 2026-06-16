package com.charmflex.app.flexiexpensesmanager.splitbill

import com.charmflex.app.flexiexpensesmanager.core.exceptions.ExceptionBase
import jakarta.servlet.http.HttpServletResponse

object SplitGroupNameRequiredException : ExceptionBase(
    HttpServletResponse.SC_BAD_REQUEST,
    "SPLIT_GROUP_NAME_REQUIRED",
    "Split group name is required."
)

object InviteCodeRequiredException : ExceptionBase(
    HttpServletResponse.SC_BAD_REQUEST,
    "SPLIT_INVITE_CODE_REQUIRED",
    "Invite code is required."
)

object InvalidInviteCodeException : ExceptionBase(
    HttpServletResponse.SC_NOT_FOUND,
    "SPLIT_INVITE_CODE_INVALID",
    "Invite code is invalid."
)

object SplitGroupNotFoundException : ExceptionBase(
    HttpServletResponse.SC_NOT_FOUND,
    "SPLIT_GROUP_NOT_FOUND",
    "Split group was not found."
)

object SplitBillNotFoundException : ExceptionBase(
    HttpServletResponse.SC_NOT_FOUND,
    "SPLIT_BILL_NOT_FOUND",
    "Split bill was not found."
)

object SplitMemberNotFoundException : ExceptionBase(
    HttpServletResponse.SC_BAD_REQUEST,
    "SPLIT_MEMBER_NOT_FOUND",
    "Split group member was not found."
)

object SplitGroupForbiddenException : ExceptionBase(
    HttpServletResponse.SC_FORBIDDEN,
    "SPLIT_GROUP_FORBIDDEN",
    "Authenticated user is not a member of this split group."
)

object SplitPaymentForbiddenException : ExceptionBase(
    HttpServletResponse.SC_FORBIDDEN,
    "SPLIT_PAYMENT_FORBIDDEN",
    "Authenticated user cannot record this split payment."
)

object SplitBillParticipantTotalException : ExceptionBase(
    HttpServletResponse.SC_BAD_REQUEST,
    "SPLIT_BILL_PARTICIPANT_TOTAL_INVALID",
    "Split bill participant amounts must equal the bill total."
)

object SplitBillRequestInvalidException : ExceptionBase(
    HttpServletResponse.SC_BAD_REQUEST,
    "SPLIT_BILL_REQUEST_INVALID",
    "Split bill request is invalid."
)

object SplitPaymentRequestInvalidException : ExceptionBase(
    HttpServletResponse.SC_BAD_REQUEST,
    "SPLIT_PAYMENT_REQUEST_INVALID",
    "Split payment request is invalid."
)
