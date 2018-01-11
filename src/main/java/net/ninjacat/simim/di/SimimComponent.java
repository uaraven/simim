package net.ninjacat.simim.di;

import dagger.Component;
import net.ninjacat.simim.app.DuplicateFinder;

import javax.inject.Singleton;

@Singleton
@Component(modules = {DatabaseModule.class})
public interface SimimComponent {
    DuplicateFinder duplicateFinder();
}
