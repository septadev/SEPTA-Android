package org.septa.android.app.favorites.edit;

import org.septa.android.app.services.apiinterfaces.model.Favorite;

public interface RenameFavoriteListener {
    void updateFavorite(Favorite favorite);

    void renameFavorite(Favorite favorite);

    void favoriteCreationFailed();
}
