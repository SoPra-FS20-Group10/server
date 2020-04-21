package ch.uzh.ifi.seal.soprafs20.rest.dto;

import ch.uzh.ifi.seal.soprafs20.entity.Tile;

import java.util.List;

public class BoardGetDTO {
    private List<Tile> grid;


    public List<Tile> getGrid() {
        return grid;
    }

    public void setGrid(List<Tile> grid) {
        this.grid = grid;
    }
}
