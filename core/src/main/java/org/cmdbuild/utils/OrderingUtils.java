package org.cmdbuild.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrderingUtils {

	public interface PositionHandler<T> {
		int getPosition(T t);

		void setPosition(T t, int p);
	}

	static public <T> void alterPosition(final List<? extends T> list, final int oldPos, final int newPos,
			final PositionHandler<T> positionHandler) {
		int maxMovePos, minMovePos, movement;
		if (newPos > oldPos) {
			maxMovePos = newPos;
			minMovePos = oldPos;
			movement = -1;
		} else {
			maxMovePos = oldPos;
			minMovePos = newPos;
			movement = 1;
		}
		Collections.sort(list, new Comparator<T>() {
			@Override
			public int compare(final T o1, final T o2) {
				return positionHandler.getPosition(o1) - positionHandler.getPosition(o2);
			}
		});
		int i = 0;
		for (final T item : list) {
			int nextPosition;
			if (i >= minMovePos && i <= maxMovePos) {
				if (i == oldPos) {
					nextPosition = newPos;
				} else {
					nextPosition = i + movement;
				}
			} else {
				nextPosition = i;
			}
			final int currentPosition = positionHandler.getPosition(item);
			if (nextPosition != currentPosition) {
				positionHandler.setPosition(item, nextPosition);
			}
			++i;
		}
	}
}
