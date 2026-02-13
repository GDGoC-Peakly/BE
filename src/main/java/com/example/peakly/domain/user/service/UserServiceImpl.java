package com.example.peakly.domain.user.service;

import com.example.peakly.domain.user.command.InitialDataCreateCommand;
import com.example.peakly.domain.user.dto.request.InitialDataCreateRequest;
import com.example.peakly.domain.user.dto.response.InitialDataCreateResponse;
import com.example.peakly.domain.user.entity.InitialData;
import com.example.peakly.domain.user.entity.User;
import com.example.peakly.domain.user.repository.InitialDataRepository;
import com.example.peakly.domain.user.repository.UserRepository;
import com.example.peakly.global.apiPayload.code.status.UserErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneId;

@Service
@Validated
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

}