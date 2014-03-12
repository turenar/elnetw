/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2014 Turenai Project
 *
 * Permission is hereby granted, free of charge,
 *  to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.twclient.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.InvalidKeyException;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.swing.GroupLayout.Alignment;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.LayoutStyle.ComponentPlacement;

/**
 * ConsumerToken
 */
public class ConsumerTokenConfigType implements ConfigType {
	private static final class JPanelMod extends JPanel {
		private static final long serialVersionUID = 6122245743341671206L;
		private JLabel componentDescriptionLabel;
		private JLabel componentConsumerKeyLabel;
		private JLabel componentConsumerSecretLabel;
		private JTextField componentConsumerKeyField;
		private JTextField componentConsumerSecretField;
		private JButton componentConsumerStoreButton;

		public JTextField getComponentConsumerKeyField() {
			if (componentConsumerKeyField == null) {
				componentConsumerKeyField = new JTextField();
			}
			return componentConsumerKeyField;
		}

		public JLabel getComponentConsumerKeyLabel() {
			if (componentConsumerKeyLabel == null) {
				componentConsumerKeyLabel = new JLabel("Consumer Key:");
			}
			return componentConsumerKeyLabel;
		}

		public JTextField getComponentConsumerSecretField() {
			if (componentConsumerSecretField == null) {
				componentConsumerSecretField = new JTextField();
			}
			return componentConsumerSecretField;
		}

		public JLabel getComponentConsumerSecretLabel() {
			if (componentConsumerSecretLabel == null) {
				componentConsumerSecretLabel = new JLabel("Consumer Secret:");
			}
			return componentConsumerSecretLabel;
		}

		public JButton getComponentConsumerStoreButton() {
			if (componentConsumerStoreButton == null) {
				componentConsumerStoreButton = new JButton("設定");
			}
			return componentConsumerStoreButton;
		}

		public JLabel getComponentDescriptionLabel() {
			if (componentDescriptionLabel == null) {
				componentDescriptionLabel = new JLabel("コンシューマーキーを設定した後は、再認証を行わないと" +
						"既存アカウントには反映されません。");
			}
			return componentDescriptionLabel;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(ConsumerTokenConfigType.class);
	private ClientProperties configProperties = ClientConfiguration.getInstance().getConfigProperties();

	@Override
	public JComponent getComponent(String configKey, String nowValue, ConfigFrame listener) {
		final JPanelMod panel = new JPanelMod();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(panel.getComponentDescriptionLabel(), Alignment.CENTER)
				.addGroup(layout.createSequentialGroup()
						.addGroup(true, layout.createParallelGroup()
								.addComponent(panel.getComponentConsumerKeyLabel())
								.addComponent(panel.getComponentConsumerSecretLabel()))
						.addGroup(layout.createParallelGroup()
								.addComponent(panel.getComponentConsumerKeyField())
								.addComponent(panel.getComponentConsumerSecretField())))
				.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
						.addComponent(panel.getComponentConsumerStoreButton())
						.addContainerGap()));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(panel.getComponentDescriptionLabel())
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup()
						.addComponent(panel.getComponentConsumerKeyLabel())
						.addComponent(panel.getComponentConsumerKeyField(), PREFERRED_SIZE, PREFERRED_SIZE,
								PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup()
						.addComponent(panel.getComponentConsumerSecretLabel())
						.addComponent(panel.getComponentConsumerSecretField(), PREFERRED_SIZE, PREFERRED_SIZE,
								PREFERRED_SIZE))
				.addComponent(panel.getComponentConsumerStoreButton()));

		String[] consumerPair;
		try {
			if (configProperties.containsKey("twitter.oauth.consumer_pair")) {
				consumerPair = configProperties.getPrivateString("twitter.oauth.consumer_pair", "X4b:mZ\"p4").split(
						":");
				panel.getComponentConsumerKeyField().setText(consumerPair[0]);
				panel.getComponentConsumerSecretField().setText(consumerPair[1]);
			}
		} catch (InvalidKeyException e) {
			logger.warn("fail getPrivateString", e);
		}

		panel.getComponentConsumerStoreButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				storeValue(panel);
				panel.getComponentConsumerStoreButton().setEnabled(false);
				panel.getComponentConsumerStoreButton().setText("OK!");
			}
		});
		panel.getComponentConsumerStoreButton().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				panel.getComponentConsumerStoreButton().setEnabled(true);
				panel.getComponentConsumerStoreButton().setText("設定");
			}
		});

		return panel;
	}

	@Override
	public String getValue(JComponent component) {
		return null;
	}

	@Override
	public boolean isPreferedAsMultiline() {
		return true;
	}

	@Override
	public boolean isValid(JComponent component) {
		return true;
	}

	private void storeValue(JPanelMod panel) {
		String consumerKey = panel.getComponentConsumerKeyField().getText().trim();
		String consumerSecret = panel.getComponentConsumerSecretField().getText().trim();
		if (!(consumerKey.isEmpty() || consumerSecret.isEmpty())) {
			String consumerPair = consumerKey + ":" + consumerSecret;
			try {
				configProperties.setPrivateString("twitter.oauth.consumer_pair", consumerPair, "X4b:mZ\"p4");
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			}
		}
	}
}
