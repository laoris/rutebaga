# Overview #

The scaffold is essentially a registry used in the initialization process of the game.

Operations used to initialize the game (referred to as "game-data") require references to objects that they have not initialized themselves, and which may not have been initialized yet.  In order to get around this, lazy-loading of objects is used.

When a reference to an object is requested, the scaffold examines its object map to see if it has been created.  If it has not, it asks the object's associated builder to (only) create a new instance of the object, adds it to its object map, and marks it as uninitialized.  It then returns a reference to the newly-created object.

# Lazy-Loading #

All operations that used these lazily-loaded, possibly-uninitialized objects must not dereference their pointers.  That is,

**Initialization methods may not dereference pointers of objects they receive from the scaffold.**

For this to hold true when operations are invoked upon the objects that the method is creating, for example in: (in the definition of an area effect)
```
Occupation summoner = scaffold.getOccupationRef("summoner");
OccupationRule rule = new OccupationRule();
rule.setValidOccupations(summoner);
```
Or, in XML:
```
<area-effect id="summonerOnlyDeath">
    <execution-rules>
        <occupation-rule occupation-refs="summoner"/>
    </execution-rules>
    ...
</area-effect>
```
As the object referred to by the pointer returned by the scaffold may not yet have been initialized, it cannot be dereferenced.  However, in this code there is no guarantee that
```
rule.setValidOccupations(summoner);
```
will not dereference the `summoner` variable.

Thus, as a general rule,

**Scaffolded classes should not dereference field pointers until the scaffolding is complete.**

An easy way to accommodate this is for all derived fields of the class to be internally lazy-loaded:
```
class OccupationRule implements Rule<Entity>
{
    private Occupation[] validOccupations;
    private Ability[] occupationAbilities; // this wouldn't really be here, but it's to illustrate an example

    public Ability[] getOccupationAbilities()
    {
        if(occupationAbilities == null)
            initializeOccupationAbilities();
    }

    ...
}
```

# Important Uses of the Scaffold #

An extremely important use of the scaffold is for stats; the scaffold frees the code from the requirement of an enumeration.

```
class SummonerFactory implements Factory<Occupation>
{
    public Occupation make()
    {
        return new Occupation();
    }

    public void initialize(Scaffold scaffold, Occupation occupation)
    {
        occupation.setStatBoost( scaffold.getStatisticId("intelligence") );
        ...
    }
}
```