<#import "template.ftl" as layout />

<@layout.noauthentication>
    <section>
        <div class="container">
            <h2>Fridge #${fridge.id}: ${fridge.name}</h2>

            <#if message??>
                <p class="flash success">${message}</p>
            </#if>
            <#if error??>
                <p class="flash error">${error}</p>
            </#if>

            <div class="fridge-summary">
                <p><strong>ID:</strong> ${fridge.id}</p>
                <p><strong>Dimensions:</strong> ${fridge.width} × ${fridge.height} × ${fridge.depth}</p>
                <p><a href="/">Back to dashboard</a></p>
            </div>

            <section class="analysis-section">
                <h3>Analysis for ${fridge.name}</h3>
                <article class="analysis-card analysis-card-wide">
                    <div class="analysis-card-header">
                        <h4>Space utilization</h4>
                        <span>${analysis.occupancyPercentage}% full</span>
                    </div>

                    <div class="meter" aria-label="Fridge occupancy">
                        <span style="width: ${analysis.occupancyPercentage}%"></span>
                    </div>

                    <dl class="metric-grid">
                        <div>
                            <dt>Capacity</dt>
                            <dd>${analysis.totalSlots}</dd>
                        </div>
                        <div>
                            <dt>Occupied</dt>
                            <dd>${analysis.occupiedSlots}</dd>
                        </div>
                        <div>
                            <dt>Free</dt>
                            <dd>${analysis.freeSlots}</dd>
                        </div>
                        <div>
                            <dt>Risk items</dt>
                            <dd>${analysis.expiryRisk.totalRiskItems}</dd>
                        </div>
                    </dl>

                    <div class="analysis-columns">
                        <div>
                            <h5>Owner usage</h5>
                            <#if analysis.ownerUsage?size == 0>
                                <p class="small-text">No placed items.</p>
                            <#else>
                                <table class="compact-table">
                                    <thead>
                                        <tr><th>Owner</th><th>Slots</th><th>Fridge share</th></tr>
                                    </thead>
                                    <tbody>
                                        <#list analysis.ownerUsage as usage>
                                            <tr>
                                                <td>${usage.ownerName}</td>
                                                <td>${usage.occupiedSlots}</td>
                                                <td>${usage.occupancyPercentage}%</td>
                                            </tr>
                                        </#list>
                                    </tbody>
                                </table>
                            </#if>
                        </div>

                        <div>
                            <h5>Expiry risk</h5>
                            <#if analysis.expiryRisk.totalRiskItems == 0>
                                <p class="small-text">No placed items expiring within seven days.</p>
                            <#else>
                                <ul class="compact-list risk-list">
                                    <#list analysis.expiryRisk.expiredItems as riskItem>
                                        <li><strong>Expired:</strong> ${riskItem.itemName} owned by ${riskItem.ownerName} (${riskItem.expiryDate})</li>
                                    </#list>
                                    <#list analysis.expiryRisk.expiringTodayItems as riskItem>
                                        <li><strong>Today:</strong> ${riskItem.itemName} owned by ${riskItem.ownerName}</li>
                                    </#list>
                                    <#list analysis.expiryRisk.expiringWithinThreeDaysItems as riskItem>
                                        <li><strong>${riskItem.daysUntilExpiry} days:</strong> ${riskItem.itemName} owned by ${riskItem.ownerName}</li>
                                    </#list>
                                    <#list analysis.expiryRisk.expiringWithinSevenDaysItems as riskItem>
                                        <li><strong>${riskItem.daysUntilExpiry} days:</strong> ${riskItem.itemName} owned by ${riskItem.ownerName}</li>
                                    </#list>
                                </ul>
                            </#if>
                        </div>
                    </div>
                </article>
            </section>

            <section>
                <h3>Contents</h3>
                <#if contents?size == 0>
                    <p>No items placed in this fridge.</p>
                <#else>
                    <table>
                        <thead>
                            <tr><th>Item</th><th>Owner</th><th>Position</th><th>Action</th></tr>
                        </thead>
                        <tbody>
                            <#list contents as record>
                                <tr>
                                    <td>
                                        <#if itemMap[record.itemId?c]?has_content>
                                            #${record.itemId} - ${itemMap[record.itemId?c].name}
                                        <#else>
                                            Unknown item
                                        </#if>
                                    </td>
                                    <td>
                                        <#if itemMap[record.itemId?c]?has_content && ownerMap[itemMap[record.itemId?c].ownerId?c]?has_content>
                                            #${itemMap[record.itemId?c].ownerId} - ${ownerMap[itemMap[record.itemId?c].ownerId?c].name}
                                        <#else>
                                            Unknown owner
                                        </#if>
                                    </td>
                                    <td>${record.x}, ${record.y}, ${record.z}</td>
                                    <td>
                                        <form method="post" action="/fridge-records/${record.id}/delete" class="inline-form">
                                            <button type="submit">Remove</button>
                                        </form>
                                    </td>
                                </tr>
                            </#list>
                        </tbody>
                    </table>
                </#if>
            </section>

            <section>
                <h3>Place item</h3>
                <form method="post" action="/fridges/${fridge.id}/place-item" class="form-panel">
                    <label>Item
                        <select name="itemId" required>
                            <option value="">Select item</option>
                            <#list items as item>
                                <option value="${item.id}">
                                    #${item.id} - ${item.name}
                                    <#if ownerMap[item.ownerId?c]?has_content>
                                        (owner #${item.ownerId} - ${ownerMap[item.ownerId?c].name})
                                    <#else>
                                        (owner #${item.ownerId})
                                    </#if>
                                </option>
                            </#list>
                        </select>
                    </label>
                    <label>X <input type="number" name="x" min="0" max="${fridge.width - 1}" required></label>
                    <label>Y <input type="number" name="y" min="0" max="${fridge.height - 1}" required></label>
                    <label>Z <input type="number" name="z" min="0" max="${fridge.depth - 1}" required></label>
                    <button type="submit">Place item</button>
                </form>
            </section>
        </div>
    </section>
</@layout.noauthentication>
