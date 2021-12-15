package com.itmo.microservices.demo.users.api.service

import com.itmo.microservices.demo.users.api.model.AppUserModel
import com.itmo.microservices.demo.users.api.model.RegistrationRequest
import com.itmo.microservices.demo.users.api.model.RegistrationResult
import com.itmo.microservices.demo.users.api.model.UserDto
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

interface UserService {
    fun findUser(name: String): AppUserModel?
    fun registerUser(request: RegistrationRequest): UserDto
    fun getAccountData(id: UUID): UserDto
    /*fun deleteUser(request: GetAccountDataRequest)*/
}