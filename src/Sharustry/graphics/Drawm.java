/*
	Copyright (c) sk7725 2020
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package Sharustry.graphics;

import arc.Core;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.game.*;
import mindustry.world.*;
/** @author sk7726 */
public class Drawm {
    /** Generates all team regions and returns the sharded team region for icon. */
    public static @Nullable TextureRegion generateTeamRegion(Block b){
        TextureRegion shardTeamTop = null;
        PixmapRegion teamr = Core.atlas.getPixmap(b.name + "-team");

        for(Team team : Team.all){
            if(team.hasPalette){
                Pixmap out = new Pixmap(teamr.width, teamr.height, teamr.pixmap.getFormat());
                out.setBlending(Pixmap.Blending.none);
                Color pixel = new Color();
                for(int x = 0; x < teamr.width; x++){
                    for(int y = 0; y < teamr.height; y++){
                        int color = teamr.getPixel(x, y);
                        int index = color == 0xffffffff ? 0 : color == 0xdcc6c6ff ? 1 : color == 0x9d7f7fff ? 2 : -1;
                        out.draw(x, y, index == -1 ? pixel.set(teamr.getPixel(x, y)) : team.palette[index]);
                    }
                }
                Texture texture  = new Texture(out);
                TextureRegion res = Core.atlas.addRegion(b.name + "-team-" + team.name, new TextureRegion(texture));

                if(team == Team.sharded){
                    shardTeamTop = res;
                }
            }
        }
        return shardTeamTop;
    }
}
