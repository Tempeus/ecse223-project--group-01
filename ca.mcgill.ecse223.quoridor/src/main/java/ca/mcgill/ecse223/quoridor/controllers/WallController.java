package ca.mcgill.ecse223.quoridor.controllers;
import ca.mcgill.ecse223.quoridor.QuoridorApplication;
import ca.mcgill.ecse223.quoridor.model.*;
import java.util.*;

public class WallController {

    /**
     * @author Tritin Truong
     * Set the tile of WallMove to a specific row and column
     * returns true if operation is successful
     *
     * @param move The subject of the move
     * @param row The target row of the move
     * @param col The target column of the move
     * @return Outcome of operation either
     * @throws UnsupportedOperationException
     */
    public static Boolean moveWall(WallMove move, int row, int col) throws UnsupportedOperationException{

        if(row<1 || row > 8 || col < 1 || col > 8){
            return false;
        }
        Tile target = ModelQuery.getTile(row,col);
        move.setTargetTile(target);
        return true;
    }

    /**
     * @author Tritin Truong
     * Shifts the position of a WallMove 1 tile left,right,up or down
     * returns true if the operation is successful
     *
     * @param side Direction of the shift (left,right,down,up)
     * @param move The subject of the shift
     * @return outcome of operation
     * @throws UnsupportedOperationException
     */
    public static Boolean shiftWall(String side, WallMove move) throws UnsupportedOperationException {
        int row = move.getTargetTile().getRow();
        int col = move.getTargetTile().getColumn();
        switch(side){
            case "left":{
                col-=1;
                break;
            }
            case "right":{
                col+=1;
                break;
            }
            case "up": {
                row-=1;
                break;
            }
            case "down":{
                row+=1;
                break;
            }
        }
        return WallController.moveWall(move,row,col);
    }

    /**
     * @author Tritin Truong
     *
     * Attempts to drop the wall at the current position
     * Also completes the moves
     *
     * @param move The subject of the drop
     * @return outcome of operation
     * @throws UnsupportedOperationException
     */
    public static Boolean dropWall(WallMove move, Player player) throws UnsupportedOperationException{
        List<Wall> placedWalls = ModelQuery.getAllWallsOnBoard();

        // validate no overlap
        for (Wall wall: placedWalls){
            if(isWallOverlap(move,wall.getMove())){
                cancelWallMove(player);
                return false;
            }
        }

        ModelQuery.getCurrentGame().addMove(move);
        ModelQuery.getCurrentGame().setWallMoveCandidate(null);

        if(player.equals(ModelQuery.getWhitePlayer())) {
            ModelQuery.getCurrentGame().getCurrentPosition().addWhiteWallsOnBoard(move.getWallPlaced());
            SwitchPlayerController.SwitchActivePlayer("white");
        }else{
            ModelQuery.getCurrentGame().getCurrentPosition().addBlackWallsOnBoard(move.getWallPlaced());
            SwitchPlayerController.SwitchActivePlayer("black");
        }

        return true;
    }

    // Return true if the wall do not overlap
    private static Boolean isWallOverlap(WallMove wall1, WallMove wall2){
        Tile t1 = wall1.getTargetTile();
        Tile t2 = wall2.getTargetTile();
        if( wall1.getWallDirection() == Direction.Vertical && wall2.getWallDirection() == Direction.Vertical){
            return t1.getColumn()==t2.getColumn() && Math.abs(t1.getRow() - t2.getRow()) <= 1;
        }
        else if(wall1.getWallDirection() == Direction.Horizontal && wall2.getWallDirection() == Direction.Horizontal){
            return  t1.getRow()==t2.getRow() && Math.abs(t1.getColumn() - t2.getColumn()) <= 1 ;
        }
        else if(wall1.getWallDirection() == Direction.Horizontal && wall2.getWallDirection() == Direction.Vertical){
            return t2.getColumn() == t1.getColumn()-1 || t2.getRow() == t1.getRow()-1;
        }
        else{
            return t1.getColumn() == t2.getColumn()+1 || t1.getRow() == t2.getRow()-1;
        }
    }

    public static boolean cancelWallMove(Player player){
        WallMove move = ModelQuery.getWallMoveCandidate();
        if(move == null){
            return false;
        }
        if(player.equals(ModelQuery.getWhitePlayer())) {
            ModelQuery.getCurrentGame().getCurrentPosition().addWhiteWallsInStock(move.getWallPlaced());
        }else{
            ModelQuery.getCurrentGame().getCurrentPosition().addBlackWallsInStock(move.getWallPlaced());
        }
        ModelQuery.getCurrentGame().setWallMoveCandidate(null);
        return true;
    }

    /**
     * @author Kate Ward
     * Attempts to rotate wall in hand
     * returns true if successful
     *
     * @return outcome of operation
     * @throws UnsupportedOperationException
     */
    public static boolean rotateWall() {
    	Game game = QuoridorApplication.getQuoridor().getCurrentGame();
        if (game.getWallMoveCandidate().equals(null)) {
        	return false;
        }
        if( game.getWallMoveCandidate().getWallDirection() == Direction.Vertical) {
        	game.getWallMoveCandidate().setWallDirection(Direction.Horizontal);
        }
        else {
        	game.getWallMoveCandidate().setWallDirection(Direction.Vertical);
        }
    	
        return true;
    }

    /**
     * @author Kate Ward
     * Attempts to grab wall from stock and hold it in hand above board
     * returns true if successful
     *
     * @return outcome of operation
     * @throws UnsupportedOperationException
     */
    public static boolean grabWall(Player player) throws UnsupportedOperationException{
        int movesSize = ModelQuery.getMoves().size();
        int moveNum;
        int roundNum;
        if (movesSize>0) {
            moveNum = ModelQuery.getMoves().get(movesSize-1).getMoveNumber();
            roundNum = ModelQuery.getMoves().get(movesSize-1).getRoundNumber();
        }
        else {
            moveNum = 0;
            roundNum = 0;
        }
        if (ModelQuery.getCurrentGame().getWallMoveCandidate()!=null) {		//wall already in hand
        	return false;
        }
        else {
    		if(player.equals(ModelQuery.getWhitePlayer()) && ModelQuery.getCurrentGame().getCurrentPosition().getWhiteWallsInStock().size()>0) {
        		List <Wall> walls = ModelQuery.getCurrentGame().getCurrentPosition().getWhiteWallsInStock();
        		Wall wall = walls.get(1);		//get(0) null for some reason
                ModelQuery.getCurrentGame().getCurrentPosition().removeWhiteWallsInStock(wall);
                //create wall move candidate
                WallMove move = new WallMove(moveNum+1, roundNum+1, player, ModelQuery.getTile(1,1), ModelQuery.getCurrentGame(), Direction.Vertical, wall);
                ModelQuery.getCurrentGame().setWallMoveCandidate(move);
                
            }else if(player.equals(ModelQuery.getBlackPlayer()) && ModelQuery.getCurrentGame().getCurrentPosition().getBlackWallsInStock().size()>0){
            	List <Wall> walls = ModelQuery.getCurrentGame().getCurrentPosition().getBlackWallsInStock();
            	Wall wall = walls.get(1);		//get(0) null for some reason
                ModelQuery.getCurrentGame().getCurrentPosition().removeBlackWallsInStock(wall);
                //create wall move candidate
                WallMove move = new WallMove(moveNum+1, roundNum, player, ModelQuery.getTile(1,1), ModelQuery.getCurrentGame(), Direction.Vertical, wall);
                ModelQuery.getCurrentGame().setWallMoveCandidate(move);
            }
        	//white player nor black player has walls in stock
            else {
            	return false;
            }
    	}
    	
        return true;
    }
}
