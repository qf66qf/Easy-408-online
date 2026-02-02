package com.easy408;

import com.easy408.entity.CardType;
import com.easy408.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CardService cardService;

    @Autowired
    private com.easy408.repository.CardRepository cardRepository;

    @Override
    public void run(String... args) throws Exception {
        if (cardRepository.count() > 0) {
            System.out.println("数据库已有数据，跳过初始化。");
            return;
        }

        System.out.println("检测到空数据库，正在初始化 408 经典真题...");

        // --- 数据结构 (DS) ---
        cardService.createCard(
            "数据结构", 
            "线性表",
            "设链表不带头结点且所有操作均在表头进行，则下列最不适合作为链栈的是（ ）。",
            "**解析**：\n\n只有表头结点的双向循环链表，若在表头进行操作，需要修改头结点的prior和next指针，复杂度为O(1)。\n\n只有表尾指针的循环单链表，若在表头插入，需遍历找到表尾节点修改next，复杂度O(n)。\n\n答案选 **C**。",
            CardType.CHOICE,
            "2019真题, 链表",
            "只有表头结点指针没有表尾指针的双向循环链表",
            "只有表尾结点指针没有表头指针的双向循环链表",
            "只有表尾结点指针没有表头指针的循环单链表",
            "只有表头结点指针没有表尾指针的单向循环链表",
            "C"
        );
        
        cardService.createCard(
            "数据结构", 
            "树与二叉树",
            "若一棵二叉树的前序遍历序列和后序遍历序列分别为 1,2,3,4 和 4,3,2,1，则该二叉树的中序遍历序列不会是（ ）。",
            "**解析**：\n\n前序：根左右；后序：左右根。\n如果前序是 1,2... 后序是 ...2,1，说明 1 是根，2 是 1 的子节点（可能是左也可能是右）。\n但是题目给出的序列完全相反，说明该二叉树每一层只有一个结点（退化成链状）。\n所以中序遍历也是这个序列的某种排列，但 1 必须在 2 的一边。\n\n本题考察特殊形态二叉树的性质。",
            CardType.CHOICE,
            "2015真题, 二叉树",
            "1,2,3,4",
            "2,3,4,1",
            "3,2,4,1",
            "4,3,2,1",
            "C"
        );

        // --- 操作系统 (OS) ---
        cardService.createCard(
            "操作系统",
            "进程管理",
            "什么是死锁？死锁产生的四个必要条件是什么？",
            "**死锁**是指两个或两个以上的进程在执行过程中，因争夺资源而造成的一种互相等待的现象，若无外力作用，它们都将无法推进下去。\n\n**四个必要条件**：\n1. **互斥条件**：资源是独占的，一个资源只能被一个进程使用。\n2. **请求和保持条件**：进程已经占有了至少一个资源，但又提出了新的资源请求，而该资源已被其他进程占有。\n3. **不剥夺条件**：进程已获得的资源在未使用完之前，不能被其他进程强行剥夺，只能主动释放。\n4. **循环等待条件**：存在一个进程等待序列 {P1, P2, ..., Pn}，其中 P1 等 P2，P2 等 P3 ... Pn 等 P1。",
            CardType.SHORT_ANSWER,
            "高频考点, 死锁",
            null, null, null, null, null
        );
        
        cardService.createCard(
             "操作系统",
             "进程管理",
             "某系统中有 3 个并发进程，都需要同类资源 4 个，试问该系统不会发生死锁的最少资源数是（ ）。",
             "**解析**：\n\n根据公式：$M = N * (K - 1) + 1$\n其中 N 是进程数(3)，K 是每个进程需要的资源数(4)。\n\n$3 * (4 - 1) + 1 = 3 * 3 + 1 = 10$。\n\n当资源数为 10 时，每个进程分到 3 个，还剩 1 个，无论分给谁，该进程都能执行完毕并释放资源。\n\n答案选 **B**。",
             CardType.CHOICE,
             "PV操作, 计算题",
             "9", "10", "11", "12", "B"
        );
        
        cardService.createCard(
            "操作系统",
            "内存管理",
            "请简述分页存储管理和分段存储管理的主要区别。",
            "1. **目的不同**：分页是出于系统管理的需要，为了提高内存利用率；分段是出于用户和程序员的需要，为了满足逻辑需求（如共享、保护）。\n2. **信息单位不同**：页是物理单位，大小固定；段是逻辑单位，大小不固定，由用户决定。\n3. **地址空间不同**：分页的地址空间是一维的（只需一个逻辑地址）；分段的地址空间是二维的（需段号+段内偏移）。",
            CardType.SHORT_ANSWER,
            "内存管理, 对比",
             null, null, null, null, null
        );

        // --- 计算机网络 (CN) ---
        cardService.createCard(
            "计算机网络",
            "传输层",
            "TCP 协议规定 HTTP 端口号为 80 的进程是（ ）。",
            "**解析**：\n\n服务端进程通常绑定固定端口监听请求（如 HTTP 的 80，HTTPS 的 443）。\n客户进程通常使用动态分配的临时端口（Ephemeral Port）。\n\n答案选 **C**。",
            CardType.CHOICE,
            "基础概念, 端口",
            "客户进程",
            "分布进程",
            "服务器进程",
            "主机进程",
            "C"
        );
        
        cardService.createCard(
            "计算机网络",
            "网络层",
            "IP地址 192.168.1.0/24 使用掩码 255.255.255.224 划分子网，最多可以划分多少个子网？每个子网可用的主机数是多少？",
            "**解析**：\n\n原掩码 /24，新掩码 .224 即 11100000，借了 3 位主机号作为子网号。\n\n1. **子网数**：$2^3 = 8$ 个。\n2. **主机数**：剩下 5 位主机号，$2^5 - 2 = 32 - 2 = 30$ 个（减去全0网络号和全1广播号）。",
             CardType.SHORT_ANSWER,
             "计算题, 子网划分",
             null, null, null, null, null
        );

        // --- 计算机组成原理 (CO) ---
        cardService.createCard(
            "计算机组成原理",
            "存储系统",
            "某计算机主存容量为 64KB，其中 ROM 区为 4KB，其余为 RAM 区，按字节编址。若使用 2K×8位 的 SRAM 芯片设计，则需要多少片？",
            "**解析**：\n\n1. 总容量 64KB。\n2. ROM 占用 4KB。\n3. RAM 容量 = 64KB - 4KB = 60KB。\n4. 芯片规格 2K×8位 = 2KB。\n5. 片数 = 60KB / 2KB = 30 片。\n\n答案选 **B**。",
            CardType.CHOICE,
            "2018真题, 存储扩展",
            "15", "30", "32", "60", "B"
        );
        
        cardService.createCard(
            "计算机组成原理",
            "指令系统",
            "什么是指令流水线？影响流水线性能的三种主要冲突是什么？",
            "**指令流水线**：将一条指令的执行过程分成若干阶段，每个阶段由独立的功能部件完成，允许多条指令在不同阶段并行执行。\n\n**三大冲突**：\n1. **结构冲突（资源冲突）**：多条指令同时争用同一资源（如同时访问内存）。\n2. **数据冲突（数据冒险）**：后一条指令需要用到前一条指令的执行结果。 \n3. **控制冲突（控制冒险）**：遇到跳转/分支指令，导致流水线断流。",
            CardType.SHORT_ANSWER,
            "流水线, 简答",
             null, null, null, null, null
        );

        System.out.println("数据初始化完成！已录入 " + cardRepository.count() + " 条真题。");
    }
}