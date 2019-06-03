package org.rockyang.blockchain.web.controller;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.rockyang.blockchain.conf.AppConf;
import org.rockyang.blockchain.core.Block;
import org.rockyang.blockchain.core.BlockChain;
import org.rockyang.blockchain.core.Transaction;
import org.rockyang.blockchain.crypto.Credentials;
import org.rockyang.blockchain.db.DBAccess;
import org.rockyang.blockchain.net.base.Node;
import org.rockyang.blockchain.utils.JsonVo;
import org.rockyang.blockchain.web.vo.TransactionVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author yangjian
 * @since 2018-04-07 上午10:50.
 */
@RestController
@RequestMapping("/chain")
public class BlockController {

	@Autowired
	private DBAccess dbAccess;
	@Autowired
	private BlockChain blockChain;
	@Autowired
	private AppConf appConf;


	@PostMapping({"", "/", "index"})
	public JsonVo index(HttpServletRequest request) {
		return JsonVo.success();
	}

	/**
	 * 启动挖矿
	 * @param request
	 * @return
	 */
	@PostMapping("/mining")
	public JsonVo mining(HttpServletRequest request) throws Exception {

		Block block = blockChain.mining();
		JsonVo vo = new JsonVo();
		vo.setCode(JsonVo.CODE_SUCCESS);
		vo.setMessage("Create a new block");
		vo.setItem(block);
		return vo;
	}

	/**
	 * 浏览区块
	 * @param request
	 * @return
	 */
	@PostMapping("/block/view")
	public JsonVo viewChain(HttpServletRequest request) {

		Optional<Block> block = dbAccess.getLastBlock();
		JsonVo success = JsonVo.success();
		if (block.isPresent()) {
			success.setItem(block.get());
		}
		return success;

	}

	/**
	 * 发送交易
	 * @param txVo
	 * @return
	 */
	@PostMapping("/transactions/new")
	public JsonVo sendTransaction(@RequestBody TransactionVo txVo) throws Exception {
		Preconditions.checkNotNull(txVo.getTo(), "Recipient is needed.");
		Preconditions.checkNotNull(txVo.getAmount(), "Amount is needed.");
		Preconditions.checkNotNull(txVo.getPrivateKey(), "Private Key is needed.");
		Credentials credentials = Credentials.create(txVo.getPrivateKey());
		Transaction transaction = blockChain.sendTransaction(
				credentials,
				txVo.getTo(),
				txVo.getAmount(),
				txVo.getData());

		//如果开启了自动挖矿，则直接自动挖矿
		if (appConf.isAutoMining()) {
			blockChain.mining();
		}
		JsonVo success = JsonVo.success();
		success.setItem(transaction);
		return success;
	}

	/**
	 * 添加节点
	 * @param node
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/node/add")
	public JsonVo addNode(@RequestBody Map<String, Object> node) throws Exception {

		Preconditions.checkNotNull(node.get("ip"), "server ip is needed.");
		Preconditions.checkNotNull(node.get("port"), "server port is need.");

		blockChain.addNode(String.valueOf(node.get("ip")), (Integer) node.get("port"));
		return JsonVo.success();
	}

	/**
	 * 查看节点列表
	 * @param request
	 * @return
	 */
	@PostMapping("node/view")
	public JsonVo nodeList(HttpServletRequest request) {

		Optional<List<Node>> nodeList = dbAccess.getNodeList();
		JsonVo success = JsonVo.success();
		if (nodeList.isPresent()) {
			success.setItem(nodeList.get());
		}
		return success;
	}

}
