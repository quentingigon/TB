package models.repositories.interfaces;

import com.google.inject.ImplementedBy;
import models.db.Site;
import models.repositories.JPASiteRepository;

@ImplementedBy(JPASiteRepository.class)
public interface SiteRepository {

	Site getByName(String name);

}
