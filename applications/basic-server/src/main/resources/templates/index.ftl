<#import "template.ftl" as layout />

<@layout.noauthentication>
    <section>
        <div class="container">
            <h2>Fridge management</h2>

            <#if message??>
                <p class="flash success">${message}</p>
            </#if>
            <#if error??>
                <p class="flash error">${error}</p>
            </#if>

            <section class="analysis-section">
                <h3>Fridge analysis</h3>
                <#if analyses?size == 0>
                    <p>No fridges available for analysis yet.</p>
                <#else>
                    <div class="analysis-grid">
                        <#list analyses as analysis>
                            <article class="analysis-card">
                                <div class="analysis-card-header">
                                    <h4><a href="/fridges/${analysis.fridgeId}">${analysis.fridgeName}</a></h4>
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

                                <h5>Owner usage</h5>
                                <#if analysis.ownerUsage?size == 0>
                                    <p class="small-text">No placed items.</p>
                                <#else>
                                    <ul class="compact-list">
                                        <#list analysis.ownerUsage as usage>
                                            <li>${usage.ownerName}: ${usage.occupiedSlots} slots (${usage.occupancyPercentage}%)</li>
                                        </#list>
                                    </ul>
                                </#if>

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
                            </article>
                        </#list>
                    </div>
                </#if>
            </section>

            <div class="grid">
                <section>
                    <h3>Fridges</h3>
                    <h4>Create fridge</h4>
                    <form method="post" action="/fridges" class="form-panel">
                        <label>Name <input type="text" name="name" required></label>
                        <label>Width <input type="number" name="width" min="1" required></label>
                        <label>Height <input type="number" name="height" min="1" required></label>
                        <label>Depth <input type="number" name="depth" min="1" required></label>
                        <button type="submit">Create fridge</button>
                    </form>

                    <h4>Current fridges</h4>
                    <#if fridges?size == 0>
                        <p>No fridges found.</p>
                    <#else>
                        <ul>
                            <#list fridges as fridge>
                                <li>
                                    <a href="/fridges/${fridge.id}">#${fridge.id} - ${fridge.name}</a>
                                    <form method="post" action="/fridges/${fridge.id}/delete" class="inline-form">
                                        <button type="submit">Delete</button>
                                    </form>
                                </li>
                            </#list>
                        </ul>
                    </#if>
                </section>

                <section>
                    <h3>Owners</h3>
                    <h4>Create owner</h4>
                    <form method="post" action="/owners" class="form-panel">
                        <label>Name <input type="text" name="name" required></label>
                        <button type="submit">Create owner</button>
                    </form>

                    <h4>Current owners</h4>
                    <#if owners?size == 0>
                        <p>No owners found.</p>
                    <#else>
                        <ul>
                            <#list owners as owner>
                                <li>
                                    #${owner.id} - ${owner.name}
                                    <form method="post" action="/owners/${owner.id}/delete" class="inline-form">
                                        <button type="submit">Delete</button>
                                    </form>
                                </li>
                            </#list>
                        </ul>
                    </#if>
                </section>

                <section>
                    <h3>Items</h3>
                    <h4>Create item</h4>
                    <form method="post" action="/items" class="form-panel">
                        <label>Name <input type="text" name="name" required></label>
                        <label>Expiry <input type="date" name="expiry" required></label>
                        <label>Owner
                            <select name="ownerId" required>
                                <option value="">Select owner</option>
                                <#list owners as owner>
                                    <option value="${owner.id}">#${owner.id} - ${owner.name}</option>
                                </#list>
                            </select>
                        </label>
                        <button type="submit">Create item</button>
                    </form>

                    <h4>Current items</h4>
                    <#if items?size == 0>
                        <p>No items found.</p>
                    <#else>
                        <ul>
                            <#list items as item>
                                <li>
                                    #${item.id} - ${item.name} (expires ${item.expiryDate?string["yyyy-MM-dd"]}) -
                                    <#if ownerMap[item.ownerId?c]?has_content>
                                        owner #${item.ownerId} - ${ownerMap[item.ownerId?c].name}
                                    <#else>
                                        owner #${item.ownerId}
                                    </#if>
                                    <form method="post" action="/items/${item.id}/delete" class="inline-form">
                                        <button type="submit">Delete</button>
                                    </form>
                                </li>
                            </#list>
                        </ul>
                    </#if>
                </section>
            </div>
        </div>
    </section>
</@layout.noauthentication>
