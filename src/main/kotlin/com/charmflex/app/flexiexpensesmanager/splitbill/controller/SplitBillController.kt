package com.charmflex.app.flexiexpensesmanager.splitbill.controller

import com.charmflex.app.flexiexpensesmanager.auth.authenticatedUser
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.CreateRemoteBillRequest
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.CreateRemotePaymentRequest
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.CreateSplitGroupRequest
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.EmptyResponse
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.JoinSplitGroupRequest
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.RegisterNotificationTokenRequest
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.RemoteBillResponse
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.RemotePaymentResponse
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.SplitGroupListResponse
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.SplitGroupResponse
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.UpdateRemoteBillRequest
import com.charmflex.app.flexiexpensesmanager.splitbill.dto.UpdateRemotePaymentRequest
import com.charmflex.app.flexiexpensesmanager.splitbill.service.SplitBillService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1")
class SplitBillController(
    private val splitBillService: SplitBillService,
) {
    @PostMapping("/split-groups")
    @ResponseStatus(HttpStatus.OK)
    fun createGroup(
        servletRequest: HttpServletRequest,
        @RequestBody request: CreateSplitGroupRequest,
    ): SplitGroupResponse {
        return splitBillService.createGroup(servletRequest.authenticatedUser(), request)
    }

    @PostMapping("/split-groups/{remoteGroupId}/invite-code")
    @ResponseStatus(HttpStatus.OK)
    fun createInviteCode(
        servletRequest: HttpServletRequest,
        @PathVariable remoteGroupId: String,
    ): SplitGroupResponse {
        return splitBillService.createInviteCode(servletRequest.authenticatedUser(), remoteGroupId)
    }

    @PostMapping("/split-groups/join")
    @ResponseStatus(HttpStatus.OK)
    fun joinGroup(
        servletRequest: HttpServletRequest,
        @RequestBody request: JoinSplitGroupRequest,
    ): SplitGroupResponse {
        return splitBillService.joinGroup(servletRequest.authenticatedUser(), request)
    }

    @GetMapping("/split-groups")
    @ResponseStatus(HttpStatus.OK)
    fun listGroups(servletRequest: HttpServletRequest): SplitGroupListResponse {
        return splitBillService.listGroups(servletRequest.authenticatedUser())
    }

    @GetMapping("/split-groups/{remoteGroupId}")
    @ResponseStatus(HttpStatus.OK)
    fun getGroup(
        servletRequest: HttpServletRequest,
        @PathVariable remoteGroupId: String,
    ): SplitGroupResponse {
        return splitBillService.getGroup(servletRequest.authenticatedUser(), remoteGroupId)
    }

    @PostMapping("/split-groups/{remoteGroupId}/bills")
    @ResponseStatus(HttpStatus.OK)
    fun createBill(
        servletRequest: HttpServletRequest,
        @PathVariable remoteGroupId: String,
        @RequestBody request: CreateRemoteBillRequest,
    ): RemoteBillResponse {
        return splitBillService.createBill(servletRequest.authenticatedUser(), remoteGroupId, request)
    }

    @PutMapping("/split-groups/{remoteGroupId}/bills/{remoteBillId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateBill(
        servletRequest: HttpServletRequest,
        @PathVariable remoteGroupId: String,
        @PathVariable remoteBillId: String,
        @RequestBody request: UpdateRemoteBillRequest,
    ): RemoteBillResponse {
        return splitBillService.updateBill(servletRequest.authenticatedUser(), remoteGroupId, remoteBillId, request)
    }

    @DeleteMapping("/split-groups/{remoteGroupId}/bills/{remoteBillId}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteBill(
        servletRequest: HttpServletRequest,
        @PathVariable remoteGroupId: String,
        @PathVariable remoteBillId: String,
    ): EmptyResponse {
        return splitBillService.deleteBill(servletRequest.authenticatedUser(), remoteGroupId, remoteBillId)
    }

    @PostMapping("/split-groups/{remoteGroupId}/payments")
    @ResponseStatus(HttpStatus.OK)
    fun createPayment(
        servletRequest: HttpServletRequest,
        @PathVariable remoteGroupId: String,
        @RequestBody request: CreateRemotePaymentRequest,
    ): RemotePaymentResponse {
        return splitBillService.createPayment(servletRequest.authenticatedUser(), remoteGroupId, request)
    }

    @PutMapping("/split-groups/{remoteGroupId}/payments/{remotePaymentId}")
    @ResponseStatus(HttpStatus.OK)
    fun updatePayment(
        servletRequest: HttpServletRequest,
        @PathVariable remoteGroupId: String,
        @PathVariable remotePaymentId: String,
        @RequestBody request: UpdateRemotePaymentRequest,
    ): RemotePaymentResponse {
        return splitBillService.updatePayment(servletRequest.authenticatedUser(), remoteGroupId, remotePaymentId, request)
    }

    @DeleteMapping("/split-groups/{remoteGroupId}/payments/{remotePaymentId}")
    @ResponseStatus(HttpStatus.OK)
    fun deletePayment(
        servletRequest: HttpServletRequest,
        @PathVariable remoteGroupId: String,
        @PathVariable remotePaymentId: String,
    ): EmptyResponse {
        return splitBillService.deletePayment(servletRequest.authenticatedUser(), remoteGroupId, remotePaymentId)
    }

    @PostMapping("/devices/notification-token")
    @ResponseStatus(HttpStatus.OK)
    fun registerNotificationToken(
        servletRequest: HttpServletRequest,
        @RequestBody request: RegisterNotificationTokenRequest,
    ): EmptyResponse {
        return splitBillService.registerNotificationToken(servletRequest.authenticatedUser(), request)
    }
}
