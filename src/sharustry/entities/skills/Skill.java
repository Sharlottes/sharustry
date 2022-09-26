package sharustry.entities.skills;

import arc.Core;

public abstract class Skill {
    public String name, displayedName, description;

    public Skill(String name) {
        this.name = name;
        this.displayedName = Core.bundle.get("stat.shar."+name+".name");
        this.description = Core.bundle.get("stat.shar."+name+".description");
    }
}
