package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.Tile;
import ch.uzh.ifi.seal.soprafs20.repository.TileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TileService {
    private final TileRepository tileRepository;

    @Autowired
    public TileService(@Qualifier("tileRepository") TileRepository tileRepository) {
        this.tileRepository = tileRepository;

        // initialise tiles
        initTiles();
    }

    public void initTiles(){
        String alpha = "abcdefghijklmnopqrstuvwxyz";

        //create and save empty tiles
        for (int i = 1; i < 4; ++i) {
            tileRepository.save(new Tile(i, null,"l"));
            tileRepository.flush();

            tileRepository.save(new Tile(i, null,"w"));
            tileRepository.flush();
        }

        //add all other stone-tile combinations ot the repo
        for(int i = 0; i < alpha.length(); i++){
            String symbol = Character.toString(alpha.charAt(i));
            for(int j = 1; j < 4; j++){
                tileRepository.save(new Tile(j, symbol,"w"));
                tileRepository.flush();

                tileRepository.save(new Tile(j, symbol,"l"));
                tileRepository.flush();
            }
        }
    }
}
