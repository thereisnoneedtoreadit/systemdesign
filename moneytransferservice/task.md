Thread-safe Money Transfer Service
   Core: AccountService.transfer(fromId, toId, amount) with atomic balance updates.
   Must handle: insufficient funds, invalid ids, high concurrency (no lost updates).
   Stretch: avoid deadlocks (lock ordering), idempotency via transferId, ledger append for audit. Tests: racey multi-threaded scenario.
