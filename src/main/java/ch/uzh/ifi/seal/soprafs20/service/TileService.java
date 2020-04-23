package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Tile;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.repository.TileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TileService {
    private final TileRepository tileRepository;

    @Autowired
    public TileService(@Qualifier("tileRepository") TileRepository tileRepository) {
        this.tileRepository = tileRepository;
    }

    public void initTiles(){
        String alpha = "abcdefghijklmnopqrstuvwxyz";

        //create and save empty tiles
        Tile tile = new Tile();
        tile.setMultiplier(1);
        tileRepository.save(tile);
        tile.setMultiplier(2);
        tileRepository.save(tile);
        tile.setMultiplier(3);
        tileRepository.save(tile);
        tileRepository.flush();

        //add all other stone-tile combinations ot the repo

        for(int i = 0; i < alpha.length();i++){
            String symbol = Character.toString(alpha.charAt(i));
            for(int j = 1; j < 4; j++){
                tile.setMultiplier(j);
                tile.setStoneSymbol(symbol);
                tileRepository.save(tile);
                tileRepository.flush();
            }
        }


    }
}
