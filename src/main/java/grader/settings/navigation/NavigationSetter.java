package grader.settings.navigation;

import grader.navigation.NavigationKind;

import java.beans.PropertyChangeListener;

import util.models.PropertyListenerRegistrar;

public interface NavigationSetter extends PropertyListenerRegistrar {

	AutomaticNavigationSetter getAutomaticNavigationSetter();

	void setAutomaticNavigationSetter(
			AutomaticNavigationSetter automaticNavigationSetter);

	NavigationFilterSetter getNavigationFilterSetter();

	void setNavigationFilterSetter(NavigationFilterSetter navigationFilterSetter);

	NavigationKind getNavigationKind();

	void setNavigationKind(NavigationKind navigationKind);

}
