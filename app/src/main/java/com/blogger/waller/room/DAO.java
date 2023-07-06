package com.blogger.waller.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.blogger.waller.room.table.EntityCategory;
import com.blogger.waller.room.table.EntityListing;
import com.blogger.waller.room.table.NotificationEntity;

import java.util.List;

@Dao
public interface DAO {

    /* table listing transaction ------------------------------------------------------------------ */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertListing(EntityListing listing);

    @Query("DELETE FROM favorite WHERE id = :id")
    void deleteListing(String id);

    @Query("DELETE FROM favorite")
    void deleteAllListing();

    @Query("SELECT * FROM favorite ORDER BY saved_date DESC LIMIT :limit OFFSET :offset")
    List<EntityListing> getAllListingByPage(int limit, int offset);

    @Query("SELECT COUNT(id) FROM favorite")
    Integer getListingCount();

    @Query("SELECT * FROM favorite WHERE id = :id LIMIT 1")
    EntityListing getListing(String id);

    /* table category transaction ------------------------------------------------------------------ */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategory(EntityCategory category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllCategories(List<EntityCategory> categories);

    @Query("SELECT * FROM category")
    List<EntityCategory> getAllCategories();

    @Query("DELETE FROM category")
    void deleteAllCategories();

    /* table notification transaction ----------------------------------------------------------- */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNotification(NotificationEntity notification);

    @Query("DELETE FROM notification WHERE id = :id")
    void deleteNotification(long id);

    @Query("DELETE FROM notification")
    void deleteAllNotification();

    @Query("SELECT * FROM notification ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    List<NotificationEntity> getNotificationByPage(int limit, int offset);

    @Query("SELECT * FROM notification WHERE id = :id LIMIT 1")
    NotificationEntity getNotification(long id);

    @Query("SELECT COUNT(id) FROM notification WHERE read = 0")
    Integer getNotificationUnreadCount();

    @Query("SELECT COUNT(id) FROM notification")
    Integer getNotificationCount();

}
