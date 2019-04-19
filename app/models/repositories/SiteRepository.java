package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.Site;

@ImplementedBy(JPASiteRepository.class)
public interface SiteRepository {

	Site getByName(String name);
}
