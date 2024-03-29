package rutebaga.controller;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import rutebaga.commons.math.EllipseBounds2D;
import rutebaga.commons.math.IntVector2D;
import rutebaga.commons.math.MutableVector2D;
import rutebaga.commons.math.Vector2D;
import rutebaga.controller.command.Command;
import rutebaga.controller.command.CreateRootContextMenuCommand;
import rutebaga.controller.command.list.ConcreteElementalList;
import rutebaga.controller.command.list.ElementalList;
import rutebaga.controller.command.list.TargetSource;
import rutebaga.controller.keyboard.ConcreteKeyBinding;
import rutebaga.controller.keyboard.KeyBinding;
import rutebaga.controller.keyboard.KeyBindingList;
import rutebaga.controller.keyboard.KeyCode;
import rutebaga.game.CooldownStatValue;
import rutebaga.model.entity.Entity;
import rutebaga.model.entity.stats.StatValue;
import rutebaga.model.entity.stats.StatisticId;
import rutebaga.model.environment.BoundsTracker;
import rutebaga.model.environment.ConcreteInstanceSet;
import rutebaga.model.environment.Environment;
import rutebaga.model.environment.Instance;
import rutebaga.model.environment.InstanceSet;
import rutebaga.model.environment.InstanceSetIdentifier;
import rutebaga.model.environment.TileConverter;
import rutebaga.model.environment.World;
import rutebaga.model.map.Tile;
import rutebaga.view.UserInterfaceFacade;
import rutebaga.view.game.MapComponent;
import rutebaga.view.game.TargetInstanceObservable;
import rutebaga.view.rwt.Menu;
import rutebaga.view.rwt.MenuItem;

public class GamePlayActionInterpreter extends MouseAdapter implements
		UserActionInterpreter, KeyListener, TargetSource
{

	private KeyBinding<Command> rebindingState;
	private boolean allowRebinding = true;
	private KeyBindingList<Command> keyPressBindings;
	private KeyBindingList<Command> keyReleaseBindings;
	private UserInterfaceFacade facade;
	private GameDaemon daemon;
	private ActionDeterminer actionDeterminer;
	private boolean rebindingIsKeyPress;
	private boolean paused;

	private World world;
	private Entity<?> avatar;
	private Collection<StatisticId> displayedStats;

	private TargetInstanceObservable observable = new TargetInstanceObservable();
	private Instance<?> target;

	public GamePlayActionInterpreter(World world, Entity<?> avatar,
			Collection<StatisticId> displayedStats)
	{
		this.world = world;
		this.avatar = avatar;
		// TODO: get rid of this
		tracker = new BoundsTracker(new EllipseBounds2D(new Vector2D(3, 3)),
				avatar);

		this.actionDeterminer = new ActionDeterminer(avatar, this);
		this.keyPressBindings = new KeyBindingList<Command>();
		this.keyReleaseBindings = new KeyBindingList<Command>();
		// TODO: redo the way movement bindings work
		MovementBindingSource movementBindings = MovementBindingSource
				.defaultBindings(avatar, this.keyPressBindings);
		initializeKeyBindings();

		this.displayedStats = displayedStats;
	}

	private class ExecuteMenuSelectionCommand implements Command
	{
		private int idx;

		public ExecuteMenuSelectionCommand(int idx)
		{
			super();
			this.idx = idx;
		}

		public void execute()
		{
			Menu menu = facade.getActiveMenu();
			if (menu != null) {
				List<? extends MenuItem> items = menu.getItems();
				items.get(idx).activate();
			}
		}

		public boolean isFeasible()
		{
			return true;
		}

	}

	private class ChangeMenuSelectionCommand implements Command
	{
		private final int shiftAmount;

		public ChangeMenuSelectionCommand(int amt)
		{
			super();
			this.shiftAmount = amt;
		}

		public void execute()
		{
			Menu menu = facade.getActiveMenu();
			if (menu != null) {
				List<? extends MenuItem> items = menu.getItems();
				int current = items.indexOf(menu.getSelectedItem());
				int newIndex = current + shiftAmount;
				if (newIndex < 0)
					newIndex = newIndex + items.size();
				else
					newIndex = newIndex % items.size();
				items.get(newIndex).select();
			}
		}

		public boolean isFeasible()
		{
			return true;
		}

	}

	private class UseCurrentMenuSelectionCommand implements Command
	{

		public void execute()
		{
			Menu menu = facade.getActiveMenu();
			if (menu == null)
				targetNextEntity();
			else {
				MenuItem selected = menu.getSelectedItem();
				selected.activate();
			}
		}

		public boolean isFeasible()
		{
			return true;
		}

	}

	private class OpenAvatarContextMenuCommand implements Command
	{

		public void execute()
		{
			target = avatar;
			retarget();
		}

		public boolean isFeasible()
		{
			return true;
		}

	}

	private void initializeKeyBindings()
	{
		// keyPressBindings.set("Explode", KeyCode.get(KeyEvent.VK_SPACE), new
		// Command() {
		// public void execute() {
		// explode();
		// }
		//
		// public boolean isFeasible() {
		// return true;
		// }
		// });

		keyPressBindings.set("AvatarMenu", KeyCode.get(KeyEvent.VK_HOME),
				new OpenAvatarContextMenuCommand());

		keyReleaseBindings.set("UseActiveMenu", KeyCode.get(KeyEvent.VK_ENTER),
				new UseCurrentMenuSelectionCommand());

		keyPressBindings.set("Close Menu", KeyCode.get(KeyEvent.VK_BACK_SPACE),
				new Command()
				{
					public void execute()
					{
						facade.clearMenuStack();
					}

					public boolean isFeasible()
					{
						return true;
					}
				});
		
		keyReleaseBindings.set("Ctxt1", KeyCode.get(KeyEvent.VK_1),
				new ExecuteMenuSelectionCommand(1));
		keyReleaseBindings.set("Ctxt2", KeyCode.get(KeyEvent.VK_2),
				new ExecuteMenuSelectionCommand(2));
		keyReleaseBindings.set("Ctxt3", KeyCode.get(KeyEvent.VK_3),
				new ExecuteMenuSelectionCommand(3));
		keyReleaseBindings.set("Ctxt4", KeyCode.get(KeyEvent.VK_4),
				new ExecuteMenuSelectionCommand(4));
		keyReleaseBindings.set("Ctxt5", KeyCode.get(KeyEvent.VK_5),
				new ExecuteMenuSelectionCommand(5));
		keyReleaseBindings.set("Ctxt6", KeyCode.get(KeyEvent.VK_6),
				new ExecuteMenuSelectionCommand(6));
		keyReleaseBindings.set("Ctxt7", KeyCode.get(KeyEvent.VK_7),
				new ExecuteMenuSelectionCommand(7));
		keyReleaseBindings.set("Ctxt8", KeyCode.get(KeyEvent.VK_8),
				new ExecuteMenuSelectionCommand(8));
		keyReleaseBindings.set("Ctxt9", KeyCode.get(KeyEvent.VK_9),
				new ExecuteMenuSelectionCommand(9));
		keyReleaseBindings.set("Ctxt0", KeyCode.get(KeyEvent.VK_0),
				new ExecuteMenuSelectionCommand(0));

		keyReleaseBindings.set("Ctxt Next", KeyCode
				.get(KeyEvent.VK_OPEN_BRACKET), new ChangeMenuSelectionCommand(-1));

		keyReleaseBindings.set("Ctxt Prev", KeyCode
				.get(KeyEvent.VK_CLOSE_BRACKET),
				new ChangeMenuSelectionCommand(1));

		keyReleaseBindings.set("Next Target", KeyCode.get(KeyEvent.VK_SPACE),
				new Command()
				{
					public void execute()
					{
						targetNextEntity();
					}

					public boolean isFeasible()
					{
						return true;
					}
				});

		Command quitCommand = new Command()
		{
			public void execute()
			{
				paused = true;
				facade.clearMenuStack();
				ConcreteElementalList list = new ConcreteElementalList();
				list.setLabel("Are you sure you want to quit?");
				list.add("Yes", new Command()
				{
					public void execute()
					{
						// TODO: save game state!!!OMGsdfsMOSFDAdsfafd
						System.exit(1);
					}

					public boolean isFeasible()
					{
						return true;
					}
				});
				list.add("No", new Command()
				{
					public void execute()
					{
						facade.clearWarningBox();
						paused = false;
					}

					public boolean isFeasible()
					{
						return true;
					}
				});
				facade.createWarningBox(list, true);
			}

			public boolean isFeasible()
			{
				return true;
			}
		};
		keyReleaseBindings.set("Quit", KeyCode.get(KeyEvent.VK_DELETE),
				quitCommand);
		keyReleaseBindings.set("Quit", KeyCode.get(KeyEvent.VK_ESCAPE),
				quitCommand);

	}

	public boolean eventsFallThrough()
	{
		// I'm a root interpreter!
		return false;
	}

	public void reactivateActionInterpreter()
	{
		// TODO Auto-generated method stub
	}

	public void installActionInterpreter(GameDaemon daemon, Game game,
			UserInterfaceFacade facade)
	{
		Collection<StatValue> stats = new ArrayList<StatValue>();
		for (StatisticId id : displayedStats)
			stats.add(avatar.getStats().getStatObject(id));

		stats.add(new CooldownStatValue(avatar));

		facade.createGamePlayScreen(avatar, observable, stats);
		daemon.registerAsKeyListener(this);
		daemon.registerAsMouseListener(this);

		this.facade = facade;
		this.daemon = daemon;

		this.actionDeterminer.setUIFacade(facade);
	}

	public void tick()
	{
		if (!paused)
		{
			if (world != null)
			{
				world.tick();
				updateTargetedTile();
				if (avatar.isDead())
					avatarDied();
			}
		}
	}

	public void uninstallActionInterpreter()
	{
		// TODO Auto-generated method stub

	}

	private void avatarDied()
	{
		paused = true;
		facade.clearMenuStack();
		ConcreteElementalList list = new ConcreteElementalList();
		list.setLabel("You are pwnd!");
		list.add("Crap", new Command()
		{
			public void execute()
			{
				daemon.deactivate(GamePlayActionInterpreter.this);
			}

			public boolean isFeasible()
			{
				return true;
			}
		});
		facade.createWarningBox(list, true);
	}

	private boolean rebindEvent(KeyEvent e)
	{
		return (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK;
	}

	public void keyPressed(KeyEvent e)
	{
		if (rebindingState != null)
		{
			completeRebinding(KeyCode.get(e.getKeyCode()));
			return;
		}
		KeyBinding<Command> binding = this.keyPressBindings.get(KeyCode.get(e));
		if (binding != null)
		{
			if (rebindEvent(e))
				startRebinding(binding);
			else
				binding.getBinding().execute();
		}
	}

	public void keyReleased(KeyEvent e)
	{
		if (rebindingState != null && e.getKeyCode() != KeyEvent.VK_SHIFT)
		{
			return;
		}
		allowRebinding = true;
		KeyBinding<Command> binding = this.keyReleaseBindings.get(KeyCode
				.get(e));
		if (binding != null)
		{
			if (rebindEvent(e))
				startRebinding(binding);
			else
				binding.getBinding().execute();
		}
	}

	public void keyTyped(KeyEvent e)
	{

	}

	private boolean checkKeyBindingCollision(KeyCode code)
	{
		found: while (true)
		{
			for (KeyBinding<Command> binding : keyPressBindings)
				if (binding.getKeyCode().equals(code))
					break found;
			for (KeyBinding<Command> binding : keyReleaseBindings)
				if (binding.getKeyCode().equals(code))
					break found;
			return false;
		}
		facade.createDialogMenu("'" + code.getKeyName()
				+ "' already bound!  Try again.", avatar, avatar);
		return true;
	}

	private void startRebinding(KeyBinding<Command> binding)
	{
		rebindingState = binding;
		allowRebinding = false;
		paused = true;
		if (keyPressBindings.get(rebindingState.getKeyCode()) != null)
		{
			rebindingIsKeyPress = true;
			keyPressBindings.remove(rebindingState);
		}
		else
		{
			rebindingIsKeyPress = false;
			keyReleaseBindings.remove(rebindingState);
		}
		facade.createDialogMenu("Press a key to bind it to\n"
				+ binding.getName() + "!", avatar, avatar);
	}

	private void completeRebinding(KeyCode code)
	{
		if (allowRebinding)
		{
			facade.popMenu();
			if (checkKeyBindingCollision(code))
				return;
			KeyBinding<Command> newBinding = new ConcreteKeyBinding<Command>(
					rebindingState.getName(), code, rebindingState.getBinding());
			if (rebindingIsKeyPress)
				keyPressBindings.set(newBinding);
			else
				keyReleaseBindings.set(newBinding);
			rebindingState = null;
			facade.createDialogMenu("Bound '"
					+ newBinding.getKeyCode().getKeyName() + "' to "
					+ newBinding.getName(), avatar, avatar);
			paused = false;
		}
	}

	private final BoundsTracker tracker;

	private void explode()
	{
		Set<Instance> set = tracker.getInstances();
		for (Instance instance : set)
		{
			if (instance.getSetIdentifier()
					.equals(InstanceSetIdentifier.ENTITY)
					|| instance.getSetIdentifier().equals(
							InstanceSetIdentifier.EFFECT))
			{
				MutableVector2D direction = new MutableVector2D(instance
						.getCoordinate().getX(), instance.getCoordinate()
						.getY());
				direction.detract(avatar.getCoordinate());
				direction.multiplyBy((3 - direction.getMagnitude()) / 3);
				instance.applyMomentum(direction);
			}
		}
	}

	private InstanceSet determineTargetSet(Point screenLocation)
	{
		Environment environment = avatar.getEnvironment();
		TileConverter converter = environment.getTileConvertor();

		Vector2D vector = MapComponent.reverseCenter(avatar, screenLocation,
				facade.getView().getWidth(), facade.getView().getHeight(),
				actionDeterminer.getTileWidth(), actionDeterminer
						.getTileHeight());

		IntVector2D tileCoordinates = converter.tileOf(vector);

		if (avatar.canSee(tileCoordinates))
		{
			InstanceSet set = new ConcreteInstanceSet();
			set.addAll(environment.instancesAt(tileCoordinates));
			for (IntVector2D adj : converter.near(tileCoordinates))
			{
				if (avatar.canSee(adj))
				{
					set.addAll(environment.instancesAt(adj));
				}
			}
			return set;
		}
		return null;
	}

	private static Tile<?> findTileAt(Instance<?> instance)
	{
		InstanceSet set = new ConcreteInstanceSet();
		set.add(instance);
		set.addAll(instance.getCoexistantInstances());
		for (Tile tile : set.getTiles())
			if (tile != null)
				return tile;
		return null;
	}

	public void mouseClicked(MouseEvent event)
	{
		switch (event.getID())
		{
		case MouseEvent.MOUSE_CLICKED:
			if (event.getButton() == MouseEvent.BUTTON1)
			{
				InstanceSet set = determineTargetSet(event.getPoint());
				if (set == null)
					target = null;
				else
					target = actionDeterminer.determineTarget(set);
			}
			else
				target = null;
			retarget();
			break;
		}
	}

	private void retarget()
	{
		facade.clearMenuStack();
		if (target != null)
		{
			ElementalList list = actionDeterminer.determineActions(target,
					daemon.getCommandQueue());
			if (list != null)
			{
				CreateRootContextMenuCommand command = new CreateRootContextMenuCommand(
						list);
				command.setUIFacade(facade);
				daemon.getCommandQueue().queueCommand(command);
			}
		}
		refreshTargetObservable();
	}

	private void refreshTargetObservable()
	{
		observable.setChanged();
		if (target != null)
		{
			Tile<?> tile = findTileAt(target);
			observable.notifyAllObservers(tile);
		}
		else
			observable.notifyAllObservers(null);
	}

	private void targetNextEntity()
	{
		InstanceSet set = new ConcreteInstanceSet();
		set.addAll(avatar.getVision().getActiveSet());

		Instance<?> newTarget = null;
		boolean targetFound = false;

		for (Entity entity : set.getEntities())
		{
			if (newTarget == null)
				newTarget = entity;
			if (entity == target)
				targetFound = true;
			else if (targetFound)
			{
				newTarget = entity;
				break;
			}
		}

		if (newTarget != target || true)
		{
			target = newTarget;
			retarget();
		}
	}

	private void updateTargetedTile()
	{
		if (target != null && target.existsInUniverse())
		{
			// TODO: lol
			IntVector2D tileCoordinate = avatar.getEnvironment()
					.getTileConvertor().tileOf(target.getCoordinate());
			if (!avatar.canSee(tileCoordinate))
			{
				target = null;
				retarget();
				return;
			}
			refreshTargetObservable();
		}
		else
		{
			target = null;
			if (facade != null)
				facade.clearMenuStack();
		}
	}

	public Instance getTarget()
	{
		return target;
	}
}
