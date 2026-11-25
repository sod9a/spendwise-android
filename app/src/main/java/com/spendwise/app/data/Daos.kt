package com.spendwise.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY type ASC, id ASC")
    fun getAll(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date LIKE :monthPrefix ORDER BY date DESC, id DESC")
    fun getByMonth(monthPrefix: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM credit_cards ORDER BY id ASC")
    fun getAll(): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE id = :id")
    suspend fun getById(id: Long): CreditCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CreditCardEntity): Long

    @Update
    suspend fun update(card: CreditCardEntity)

    @Delete
    suspend fun delete(card: CreditCardEntity)

    @Query("DELETE FROM credit_cards")
    suspend fun deleteAll()
}

@Dao
interface SavingsDao {
    @Query("SELECT * FROM savings ORDER BY id ASC")
    fun getAll(): Flow<List<SavingsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(savings: SavingsEntity): Long

    @Update
    suspend fun update(savings: SavingsEntity)

    @Delete
    suspend fun delete(savings: SavingsEntity)

    @Query("DELETE FROM savings")
    suspend fun deleteAll()
}

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY id ASC")
    fun getAll(): Flow<List<LoanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loan: LoanEntity): Long

    @Update
    suspend fun update(loan: LoanEntity)

    @Delete
    suspend fun delete(loan: LoanEntity)

    @Query("DELETE FROM loans")
    suspend fun deleteAll()
}
