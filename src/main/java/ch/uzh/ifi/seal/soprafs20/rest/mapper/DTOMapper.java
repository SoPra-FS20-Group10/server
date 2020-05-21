package ch.uzh.ifi.seal.soprafs20.rest.mapper;

import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.rest.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g., UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for creating information (POST).
 */
@Mapper
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")
    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "token", target = "token")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "birthday", target = "birthday")
    @Mapping(source = "cakeDay", target = "cakeDay")
    @Mapping(source = "overallScore", target = "overallScore")
    @Mapping(source = "playedGames", target = "playedGames")
    @Mapping(source = "wonGames", target = "wonGames")
    @Mapping(source = "winPercentage", target = "winPercentage")
    @Mapping(source = "history", target = "historyString")
    @Mapping(source = "historyTime", target = "historyTimeString")
    UserGetDTO convertEntityToUserGetDTO(User user);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "birthday", target = "birthday")
    User convertUserPutDTOtoEntity(UserPutDTO user);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "status", target = "status")
    GameGetDTO convertEntityToGameGetDTO(Game game);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "password", target = "password")
    Game convertGamePostDTOToEntity(GamePostDTO game);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "score", target = "score")
    PlayerGetDTO convertEntityToPlayerGetDTO(Player player);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "symbol", target = "symbol")
    @Mapping(source = "value", target = "value")
    StoneGetDTO convertEntityToStoneGetDTO(Stone stone);

    @Mapping(source = "stoneSymbol", target = "stoneSymbol")
    @Mapping(source = "value", target = "value")
    @Mapping(source = "multiplier", target = "multiplier")
    @Mapping(source = "multivariant", target = "multivariant")
    TileGetDTO convertEntityToTileGetDTO(Tile tile);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "time", target = "time")
    @Mapping(source = "message", target = "message")
    Message convertMessageDTOtoEntity(MessageDTO messageDTO);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "time", target = "time")
    @Mapping(source = "message", target = "message")
    MessageDTO convertEntityToMessageDTO(Message message);

    @Mapping(source = "messages", target = "messages")
    ChatGetDTO convertEntityToChatGetDTO(Chat chat);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "word", target = "word")
    @Mapping(source = "value", target = "value")
    WordGetDTO convertEntityToWordGetDTO(Word word);
}
