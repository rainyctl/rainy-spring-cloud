# Distributed Transactions (Seata)

## The Problem
In distributed systems, local transactions (`@Transactional`) cannot guarantee consistency across multiple services. If Order Service fails after Product Service commits, data is inconsistent.

## Seata Architecture
1.  **TC (Transaction Coordinator)**: Seata Server. Maintains global state.
2.  **TM (Transaction Manager)**: Initiator (Order Service). Begins global tx.
3.  **RM (Resource Manager)**: Participants (Order/Product DBs).

## AT Mode Workflow
**Phase 1 (Prepare)**:
- Execute business SQL.
- Save **Before/After Images** to `undo_log` table.
- Commit local transaction.

**Phase 2 (Commit/Rollback)**:
- **Commit**: Async delete `undo_log`.
- **Rollback**: Use images to restore data.

## Setup Guide
1.  **Server**: Run `seata-server.sh`.
2.  **Client**: Add `spring-cloud-starter-alibaba-seata`.
3.  **Config**: `file.conf` pointing to Seata Server.
4.  **Database**: Create `undo_log` table in ALL databases.
5.  **Code**: Add `@GlobalTransactional` to the entry method.
