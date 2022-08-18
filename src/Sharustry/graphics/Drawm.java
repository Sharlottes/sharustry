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
import mindustry.graphics.MultiPacker;
import mindustry.world.*;
/** @author sk7726 */
public class Drawm {
    /** Generates all team regions and returns the sharded team region for icon. */
    public static void generateTeamRegion(MultiPacker packer, Block b){
        PixmapRegion teamr = Core.atlas.getPixmap(b.name + "-team");

        for(Team team : Team.all){
            if(team.hasPalette){
                Pixmap out = new Pixmap(teamr.width, teamr.height);
                for(int x = 0; x < teamr.width; x++){
                    for(int y = 0; y < teamr.height; y++){
                        int color = teamr.getRaw(x, y);
                        int index = color == 0xffffffff ? 0 : color == 0xdcc6c6ff ? 1 : color == 0x9d7f7fff ? 2 : -1;
                        out.setRaw(x, y, index == -1 ? teamr.getRaw(x, y) : team.palettei[index]);
                    }
                }
                packer.add(MultiPacker.PageType.main, b.name + "-team-" + team.name, out);
            }
        }

        //force reloadCounter of team region
        b.load();
    }
}
