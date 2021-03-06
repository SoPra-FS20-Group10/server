package ch.uzh.ifi.seal.soprafs20.rest.mapper;

import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.rest.dto.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation works.
 */
class DTOMapperTest {

    @Test
    void testCreateUser_fromUserPostDTO_toUser_success() {
        // create UserPostDTO
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("username");

        // MAP -> Create user
        User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // check content
        assertEquals(userPostDTO.getUsername(), user.getUsername());
    }

    @Test
    public void testGetUser_fromUser_toUserGetDTO_success() {
        // create User
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("1");

        // MAP -> Create UserGetDTO
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

        // check content
        assertEquals(user.getId(), userGetDTO.getId());
        assertEquals(user.getUsername(), userGetDTO.getUsername());
        assertEquals(user.getStatus(), userGetDTO.getStatus());
    }

    @Test
    public void testPostGame_fromGamePostDTO_to_Game_success() {
        // create GamePostDTO
        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setOwnerId(1);
        gamePostDTO.setName("TestName");
        gamePostDTO.setPassword("TestPassword");

        // MAP -> Create Game
        Game game = DTOMapper.INSTANCE.convertGamePostDTOToEntity(gamePostDTO);

        // check content
        assertEquals(game.getName(), gamePostDTO.getName());
        assertEquals(game.getPassword(), gamePostDTO.getPassword());
    }

    @Test
    public void test_Entity_to_PlayerGetDTO(){
        //create Player
        Player player = new Player();
        player.setUsername("username");
        player.setStatus(PlayerStatus.NOT_READY);
        player.setScore(300);

        // MAP

        PlayerGetDTO playerGetDTO = DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(player);

        // check content


        assertEquals(player.getScore(),playerGetDTO.getScore());
        assertEquals(player.getUsername(),playerGetDTO.getUsername());
        assertEquals(player.getStatus(),playerGetDTO.getStatus());
        assertEquals(player.getScore(),playerGetDTO.getScore());

    }

    @Test
    public void test_convertEntityToStoneGetDTO(){
        //create Stone
        Stone stone = new Stone();
        stone.setSymbol("a");
        stone.setValue(2);

        // MAP

        StoneGetDTO stoneGetDTO = DTOMapper.INSTANCE.convertEntityToStoneGetDTO(stone);

        // check content
        assertEquals(stone.getSymbol(),stoneGetDTO.getSymbol());
        assertEquals(stone.getValue(),stoneGetDTO.getValue());
    }

    @Test
    public void test_convertEntityToTileGetDTO(){
        Tile tile = new Tile();
        tile.setStoneSymbol("a");
        tile.setMultiplier(1);
        tile.setMultivariant("ab");

        // MAP
        TileGetDTO tileGetDTO = DTOMapper.INSTANCE.convertEntityToTileGetDTO(tile);


        // check content
        assertEquals(tile.getStoneSymbol(),tileGetDTO.getStoneSymbol());
        assertEquals(tile.getMultiplier(),tileGetDTO.getMultiplier());
        assertEquals(tile.getMultivariant(),tileGetDTO.getMultivariant());
    }

    @Test
    public void test_convertEntityToMessageDTO(){
        Message message = new Message();
        message.setTime(1L);
        message.setMessage("asdf");
        message.setUsername("adsfa");

        // MAP


        MessageDTO messageDTO = DTOMapper.INSTANCE.convertEntityToMessageDTO(message);

        // check content

        assertEquals(message.getTime(),messageDTO.getTime());
        assertEquals(message.getUsername(),messageDTO.getUsername());
        assertEquals(message.getMessage(),messageDTO.getMessage());
    }

    @Test
    public void test_convertEntityToWordGetDTO(){
        Word word = new Word();
        word.setValue(2);
        word.setWord("word");

        // MAP

        WordGetDTO wordGetDTO = DTOMapper.INSTANCE.convertEntityToWordGetDTO(word);

        // check content

        assertEquals(word.getValue(),wordGetDTO.getValue());
        assertEquals(word.getWord(),wordGetDTO.getWord());
    }
}
