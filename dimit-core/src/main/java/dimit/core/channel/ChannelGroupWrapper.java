/**
 * 
 */
package dimit.core.channel;

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import dimit.core.Dimiter;
import dimit.core.StoreConst;
import dimit.store.ChannelGroup;
import dimit.store.ChannelType;
import dimit.store.conf.ChannelGroupConf;
import dimit.store.conf.MagicFlag;
import dimit.store.sys.Const;
import dimit.store.sys.DimitPath;
import dimit.store.sys.DimitStoreSystem;
import dimit.store.util.IDUtil;

/**
 * @author dzh
 * @date Mar 21, 2018 3:58:31 PM
 * @version 0.0.1
 */
public class ChannelGroupWrapper implements StoreWrapper<ChannelGroup, ChannelGroupConf> {

    private List<ChannelWrapper> channel;
    private ChannelGroupConf conf;
    private ChannelGroup store;

    private ChannelSelector selector;
    private Dimiter dimiter;

    private ChannelGroupWrapper(Dimiter dimiter) {
        this.dimiter = dimiter;
        channel = Collections.synchronizedList(new LinkedList<ChannelWrapper>());
        selector = new SimpleChannelSelector(this);
    }

    public static final ChannelGroupWrapper init(Dimiter dimiter, String cid) throws IOException {
        DimitStoreSystem dss = dimiter.storeSystem();
        DimitPath dimitPath = dss.getPath(StoreConst.PATH_CONF, dimiter.dimit().conf().getId());

        ChannelGroupWrapper group = new ChannelGroupWrapper(dimiter);
        group.conf = dimitPath.newPath(cid).<ChannelGroupConf> toStore(ChannelGroupConf.class);

        if (group.conf == null) return null;

        // create ChannelGroup
        long ct = System.currentTimeMillis();
        ChannelGroup store = ChannelGroup.newBuilder().setId(IDUtil.storeID(MagicFlag.CHANNEL_GROUP)).setCid(cid).setCt(ct).setMt(ct)
                .setV(Const.V).setDimit(dimiter.id()).build();
        group.store = store;

        return group;
    }

    public List<ChannelWrapper> select(String... tags) {
        return selector.select(tags);
    }

    /**
     * @param cid
     *            ChannelConf's id
     * @param type
     * @return
     * @throws IOException
     */
    public ChannelWrapper newChannel(String cid, ChannelType type) throws IOException {
        ChannelWrapper channel = ChannelWrapper.init(dimiter, this, cid, type);
        if (channel == null) throw new IOException("Couldn't newChannel:" + cid);
        channel().add(channel);
        return channel;
    }

    public List<ChannelWrapper> channel() {
        return channel;
    }

    @Override
    public void close() throws IOException {
        for (ChannelWrapper ch : channel) {
            ch.close();
        }
    }

    public boolean contain(String id) {
        for (ChannelWrapper ch : channel) {
            if (ch.id().equals(id)) return true;
        }
        return false;
    }

    @Override
    public ChannelGroup store() {
        return store;
    }

    @Override
    public ChannelGroupConf conf() {
        return conf;
    }

    public ChannelSelector selector() {
        return selector;
    }

    @Override
    public String name() {
        return conf.getName();
    }

    @Override
    public String id() {
        return store.getId();
    }

    @Override
    public Dimiter dimiter() {
        return this.dimiter;
    }

    @Override
    public void handle(WatchKey key, WatchEvent<?> event) {
        // TODO Auto-generated method stub

    }

}
