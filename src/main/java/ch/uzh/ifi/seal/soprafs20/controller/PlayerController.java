package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.rest.dto.PlayerGetDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserTokenDTO;
import ch.uzh.ifi.seal.soprafs20.rest.mapper.DTOMapper;
import ch.uzh.ifi.seal.soprafs20.service.PlayerService;
import org.aspectj.weaver.ast.Not;
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
        Player player = playerService.getPlayer(playerId);

        if(player == null){
            throw new NotFoundException("the player could not be found");
        }

        return DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(player);
    }

    @PutMapping("/players/{playerId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void readyPlayer(@PathVariable("playerId")long playerId, @RequestBody UserTokenDTO userTokenDTO) {
        // parse input into String
        String token = userTokenDTO.getToken();

        if(token == null){
            throw new NotFoundException("the token is empty");
        }

        // ready player
        playerService.readyPlayer(playerId, token);
    }
}