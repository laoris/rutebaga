# Velocity-Based Model #

Each instance that has location contains a vector representing its velocity.  This vector is managed solely by the environment; that is, the instance cannot directly alter it.

Affecting this vector is done through a public operation that applies a force to the instance.

Before forwarding ticks, the environment iterates through its instances.  Based on each instance's velocity, it determines:

  * if the instance is about to cross a tile boundary
  * if so, whether or not it is able to enter that tile

And accordingly,

  * moves the instance, or
  * does not move the instance

Regardless, the velocity vector is reset after this has completed.

# Momentum-Based Model #

There are two separate components of the momentum-based model.  The two parts are:

  * impulse-based applications, which act without regard to the instance's current movement
  * velocity-based applications, which act like a viscous fluid

We must define the following units:

  * time: tick (t)
  * mass: mass unit (mu)
  * force: force unit (N)  (what, did you think I'd make it 'fu'?)

The environment's physics resolution cycle commences as follows:

  * velocity resultant from momentum (**v<sub>p</sub>**) is calculated
  * net velocity (**v<sub>net</sub>**) is calculated from applied velocity and velocity resultant from momentum
  * applied velocity is reset
  * the momentum is adjusted based on the coefficient of friction available and the mass of the instance
  * if the instance is able to move, it is moved a distance that is directly proportional to its velocity
  * if the instance is not able to move, its momentum is reset to zero