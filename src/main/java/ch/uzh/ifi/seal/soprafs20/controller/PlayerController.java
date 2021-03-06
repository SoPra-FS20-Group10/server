package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.rest.dto.PlayerGetDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserTokenDTO;
import ch.uzh.ifi.seal.soprafs20.rest.mapper.DTOMapper;
import ch.uzh.ifi.seal.soprafs20.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class PlayerController {
    private final PlayerService playerService;

    PlayerController(PlayerService playerService){this.playerService = playerService;}

    @GetMapping("/players/{playerId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PlayerGetDTO getPlayer(@PathVariable("playerId")long playerId){
        // fetch player from db
        Player player = playerService.getPlayer(playerId);

        // parse entity into PlayerGetDTO and return
        return DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(player);
    }

    @PutMapping("/players/{playerId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void readyPlayer(@PathVariable("playerId")long playerId, @RequestBody UserTokenDTO userTokenDTO) {
        // parse input into String
        String token = userTokenDTO.getToken();

        // check if token exists
        if(token == null){
            throw new NotFoundException("the token is empty");
        }

        // ready player
        playerService.readyPlayer(playerId, token);
    }
}