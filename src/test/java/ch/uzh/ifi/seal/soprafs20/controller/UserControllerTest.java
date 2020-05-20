package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserGetDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserPostDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserPutDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserTokenDTO;
import ch.uzh.ifi.seal.soprafs20.rest.mapper.DTOMapper;
import ch.uzh.ifi.seal.soprafs20.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void getUser_validInput_withoutHistory() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setToken("testToken");
        user.setUsername("testUsername");
        user.setStatus(UserStatus.OFFLINE);
        user.setBirthday(new Date());
        user.setCakeDay(new Date());
        user.setOverallScore(150);
        user.setPlayedGames(200);
        user.setWonGames(50);
        user.setWinPercentage();

        // when -> then: return user
        given(userService.getUser(Mockito.anyLong())).willReturn(user);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder getRequest = get("/users/1")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.token", is(user.getToken())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
                .andExpect(jsonPath("$.overallScore", is(user.getOverallScore())))
                .andExpect(jsonPath("$.playedGames", is(user.getPlayedGames())))
                .andExpect(jsonPath("$.wonGames", is(user.getWonGames())))
                .andExpect(jsonPath("$.winPercentage", is(0.25)))
                .andExpect(jsonPath("$.historyString", is(user.getHistory())));
    }

    @Test
    public void getUser_validInput_withHistory() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setToken("testToken");
        user.setUsername("testUsername");
        user.setStatus(UserStatus.OFFLINE);
        user.setBirthday(new Date());
        user.setCakeDay(new Date());
        user.setOverallScore(150);
        user.setPlayedGames(200);
        user.setWonGames(50);
        user.setWinPercentage();
        user.setHistory("10 15 20");

        // when -> then: return user
        given(userService.getUser(Mockito.anyLong())).willReturn(user);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder getRequest = get("/users/1")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.token", is(user.getToken())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
                .andExpect(jsonPath("$.overallScore", is(user.getOverallScore())))
                .andExpect(jsonPath("$.playedGames", is(user.getPlayedGames())))
                .andExpect(jsonPath("$.wonGames", is(user.getWonGames())))
                .andExpect(jsonPath("$.winPercentage", is(0.25)))
                .andExpect(jsonPath("$.historyString", is(user.getHistory())))
                .andExpect(jsonPath("$.historyList[0]", is(10)))
                .andExpect(jsonPath("$.historyList[1]", is(15)))
                .andExpect(jsonPath("$.historyList[2]", is(20)));
    }

    @Test
    public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
        // given
        User user = new User();
        user.setId(1);
        user.setUsername("firstname@lastname");
        user.setPassword("testPassword");
        user.setStatus(UserStatus.OFFLINE);

        List<User> allUsers = Collections.singletonList(user);

        // this mocks the UserService -> we define above what the userService should return when getUsers() is called
        given(userService.getUsers()).willReturn(allUsers);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(notNullValue())))
                .andExpect(jsonPath("$[0].username", is(user.getUsername())))
                .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
    }

    @Test
    public void createUser_validInput_userCreated() throws Exception {
        // given
        User user = new User();
        user.setUsername("testUsername");
        user.setPassword("testPassword");

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("testPassword");

        given(userService.createUser(Mockito.any())).willReturn(user);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest).andExpect(status().isCreated());
    }

    @Test
    public void loginUser_validInput() throws Exception {
        // given
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("testPassword");

        User user = new User();
        user.setId(1);
        user.setToken("testToken");
        user.setUsername("testUsername");
        user.setStatus(UserStatus.ONLINE);
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

        // when -> then: return user
        given(userService.loginUser(Mockito.any())).willReturn(ResponseEntity.status(HttpStatus.OK).body(userGetDTO));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(putRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.token", is(user.getToken())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }

    @Test
    public void logoutUser_validInput() throws Exception {
        UserTokenDTO userTokenDTO = new UserTokenDTO();
        userTokenDTO.setToken("testToken");

        // do the request + validate the result
        MockHttpServletRequestBuilder patchRequest = patch("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTokenDTO));

        // then
        mockMvc.perform(patchRequest)
                .andExpect(status().isOk());
    }

    @Test
    public void logoutUser_invalidInput_noToken() throws Exception {
        UserTokenDTO userTokenDTO = new UserTokenDTO();
        userTokenDTO.setToken(null);

        // do the request + validate the result
        MockHttpServletRequestBuilder patchRequest = patch("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTokenDTO));

        // then
        mockMvc.perform(patchRequest).andExpect(status().isNotFound());
    }

    @Test
    public void deleteUser_validInput() throws Exception {
        // do the request + validate the result
        MockHttpServletRequestBuilder deleteRequest = delete("/users/1")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(deleteRequest).andExpect(status().isNoContent());
    }

    @Test
    public void updateUser_validInput() throws Exception {
        // given
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("bla");
        userPutDTO.setPassword("bla");

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        // then
        mockMvc.perform(putRequest).andExpect(status().isNoContent());
    }


    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        }
        catch (JsonProcessingException e) {
            throw new SopraServiceException(String.format("The request body could not be created.%s", e.toString()));
        }
    }
}