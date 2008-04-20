package rutebaga.scaffold.builders;

import java.awt.Image;
import java.util.ArrayList;

import rutebaga.appearance.AppearanceManagerDefinition;
import rutebaga.appearance.StaticAppearanceDef;
import rutebaga.model.environment.ConcreteInstanceType;
import rutebaga.model.environment.Instance;
import rutebaga.model.environment.InstanceType;
import rutebaga.model.environment.MovementAttributes;
import rutebaga.model.environment.appearance.Appearance;
import rutebaga.scaffold.MasterScaffold;

public abstract class InstanceBuilder<T extends ConcreteInstanceType> extends
		ConfigFileBuilder
{

	public void initialize(String id, Object object, MasterScaffold scaffold)
	{
		ConcreteInstanceType instance = (ConcreteInstanceType) object;

		instance.setMovementAttributes((MovementAttributes) getObjectFor(id,
				"movementAttr", scaffold));

		String appManDesc = getProperty(id, "appearance");
		AppearanceManagerDefinition appDef = AppearanceDefFactory.getInstance()
				.get(appManDesc, scaffold);
		if (appDef != null)
			instance.setAppearanceDefinition(appDef);
		else
			instance.setAppearanceDefinition(new StaticAppearanceDef());

	}
}
